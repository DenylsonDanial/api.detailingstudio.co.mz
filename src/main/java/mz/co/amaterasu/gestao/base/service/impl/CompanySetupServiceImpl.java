package mz.co.amaterasu.gestao.base.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.dto.BusinessSettingDTO;
import mz.co.amaterasu.gestao.base.dto.CompanySetupDTO;
import mz.co.amaterasu.gestao.base.entity.Tenant;
import mz.co.amaterasu.gestao.base.entity.UserTenant;
import mz.co.amaterasu.gestao.base.repository.TenantRepository;
import mz.co.amaterasu.gestao.base.repository.UserTenantRepository;
import mz.co.amaterasu.gestao.base.service.BusinessSettingService;
import mz.co.amaterasu.gestao.base.service.CompanySetupService;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import mz.co.amaterasu.gestao.user.dto.UserDTO;
import mz.co.amaterasu.gestao.user.dto.projection.PUserDTO;
import mz.co.amaterasu.gestao.user.service.UserService;
import mz.co.amaterasu.gestao.util.*;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CompanySetupServiceImpl implements CompanySetupService {

    @Value("${app.db.hostname}")
    private String dbHostname;
    @Value("${app.db.port}")
    private String dbPort;
    @Value("${spring.datasource.username}")
    private String dbUsername;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final TenantRepository tenantRepository;
    private final UserTenantRepository userTenantRepository;
    private final BusinessSettingService businessSettingService;
    private final UserService userService;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    private TenantDataSourceManager tenantDataSourceManager;
    @Autowired
    private DataSource masterDataSource;

    /**
     * O provisionamento nao pode correr numa unica transacao: a ligacao JDBC
     * fica presa ao master no inicio do pedido, e o CREATE DATABASE nao reverte.
     * Por isso: (1) gravar tenant/user no master e fazer commit, (2) criar a BD
     * e migrar, (3) gravar definicoes numa transacao nova ja no tenant.
     */
    @Override
    public void companySetup(CompanySetupDTO companySetupDTO) throws ApiRequestException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, SQLException, IOException {

        BusinessSettingDTO businessSettingDTO = companySetupDTO.getBusinessSetting();
        businessSettingDTO.setActive(true);

        String tenantId = resolveTenantId(businessSettingDTO);
        businessSettingDTO.setDbTenantName(tenantId);

        UserDTO userDTO = companySetupDTO.getUser();
        long userId = companySetupDTO.getUserId();

        TransactionTemplate masterTx = new TransactionTemplate(transactionManager);
        try {
            masterTx.executeWithoutResult(status -> {
                Tenant tenant = new Tenant();
                tenant.setTenantName(businessSettingDTO.getBusinessName());
                tenant.setSlug(tenantId);
                tenant.setActive(true);
                tenantRepository.save(tenant);

                PUserDTO pUserDTO;
                try {
                    if (userId > 0) {
                        pUserDTO = userService.findById(userId);
                    } else {
                        pUserDTO = userService.save(userDTO);
                    }
                } catch (ApiRequestException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ApiRequestException("Falha ao associar o utilizador a empresa: " + e.getMessage());
                }
                userTenantRepository.save(new UserTenant(null, pUserDTO.getUsername(), tenantId));
            });
        } catch (DataIntegrityViolationException e) {
            throw new ApiRequestException("Nao foi possivel criar a empresa. Tente um nome diferente.");
        }

        createTenantDatabaseAndSetup(tenantId);

        String previousTenant = TenantContext.getCurrentTenant();
        try {
            TenantContext.setCurrentTenant(tenantId);
            // Transacao nova: a ligacao e obtida ja com o TenantContext do tenant.
            businessSettingService.create(businessSettingDTO);
        } finally {
            TenantContext.setCurrentTenant(previousTenant);
        }
    }

    /**
     * O nome da base de dados nao pode ser parametrizado num CREATE DATABASE, por
     * isso e validado contra uma lista branca de caracteres antes de ser concatenado.
     */
    private static final Pattern VALID_TENANT_NAME = Pattern.compile("^[a-z][a-z0-9_]{2,62}$");

    /**
     * Usa o valor enviado se for valido; caso contrario deriva do nome da empresa
     * e resolve colisoes com sufixos _2, _3, ...
     */
    private String resolveTenantId(BusinessSettingDTO dto) {
        String requested = dto.getDbTenantName();
        if (requested != null && !requested.isBlank()) {
            String trimmed = requested.trim().toLowerCase();
            if (!VALID_TENANT_NAME.matcher(trimmed).matches()) {
                throw new ApiRequestException(
                        "Identificador de empresa invalido. Use apenas letras minusculas, digitos e underscore, "
                                + "comecando por uma letra.");
            }
            if (tenantRepository.existsBySlug(trimmed)) {
                throw new ApiRequestException("Ja existe uma empresa com este identificador de base de dados.");
            }
            return trimmed;
        }

        String base = slugifyBusinessName(dto.getBusinessName());
        String candidate = base;
        int suffix = 2;
        while (tenantRepository.existsBySlug(candidate)) {
            String withSuffix = base + "_" + suffix;
            if (withSuffix.length() > 63) {
                withSuffix = base.substring(0, Math.max(1, 63 - ("_" + suffix).length())) + "_" + suffix;
            }
            candidate = withSuffix;
            suffix++;
            if (suffix > 9999) {
                throw new ApiRequestException("Nao foi possivel gerar um identificador unico para a empresa.");
            }
        }
        return candidate;
    }

    static String slugifyBusinessName(String businessName) {
        if (businessName == null || businessName.isBlank()) {
            throw new ApiRequestException("Nome da empresa e obrigatorio para gerar o identificador.");
        }
        String normalized = Normalizer.normalize(businessName.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        String slug = normalized.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        if (slug.isEmpty() || !Character.isLetter(slug.charAt(0))) {
            slug = "c_" + slug;
        }
        slug = slug.replaceAll("[^a-z0-9_]", "");
        if (slug.length() < 3) {
            slug = (slug + "_co");
        }
        if (slug.length() > 63) {
            slug = slug.substring(0, 63).replaceAll("_+$", "");
        }
        if (!VALID_TENANT_NAME.matcher(slug).matches()) {
            throw new ApiRequestException(
                    "Nao foi possivel derivar um identificador valido a partir do nome da empresa.");
        }
        return slug;
    }

    public void createTenantDatabaseAndSetup(String dbName) throws SQLException, IOException {
        if (dbName == null || !VALID_TENANT_NAME.matcher(dbName).matches()) {
            throw new ApiRequestException(
                    "Identificador de empresa invalido. Use apenas letras minusculas, digitos e underscore, "
                            + "comecando por uma letra.");
        }

        String createDbSQL = "CREATE DATABASE " + dbName;
        try (Connection connection = masterDataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createDbSQL);
        }

        // Configure the new DataSource for this tenant
        DataSource tenantDataSource = DataSourceUtil.createDataSource(
                "jdbc:postgresql://" + dbHostname + ":" + dbPort + "/" + dbName,
                dbUsername,
                dbPassword
        );
        tenantDataSourceManager.addDataSource(dbName, tenantDataSource);

        Flyway.configure()
                .dataSource(tenantDataSource)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }
}
