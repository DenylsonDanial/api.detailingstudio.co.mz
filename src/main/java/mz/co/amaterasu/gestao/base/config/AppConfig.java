package mz.co.amaterasu.gestao.base.config;

import mz.co.amaterasu.gestao.util.TenantContext;
import mz.co.amaterasu.gestao.util.TenantDataSourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
public class AppConfig {

    /** Autor atribuido as escritas feitas fora de um pedido autenticado. */
    private static final String SYSTEM_AUDITOR = "system";

    @Autowired
    private TenantDataSourceManager tenantDataSourceManager;
    @Autowired
    private DataSource masterDataSource;

    /**
     * O encaminhamento por empresa e resolvido em
     * {@link TenantRoutingDataSource#determineTargetDataSource()}, que consulta o
     * {@link TenantDataSourceManager} e cria o pool na primeira utilizacao. O mapa
     * de destinos fica so com o master, que serve de destino por omissao enquanto
     * nao ha empresa no contexto.
     */
    @Bean
    public DataSource routingDataSource() {
        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource(tenantDataSourceManager);

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(TenantContext.getMasterDb(), masterDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        return routingDataSource;
    }

    /** Preenche created_by e updated_by nas entidades que estendem Auditable. */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return Optional.of(SYSTEM_AUDITOR);
            }
            return Optional.of(auth.getName());
        };
    }

}
