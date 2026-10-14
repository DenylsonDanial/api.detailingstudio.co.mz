package mz.co.amaterasu.gestao.base.config;

import mz.co.amaterasu.gestao.util.DataSourceUtil;
import mz.co.amaterasu.gestao.util.TenantContext;
import mz.co.amaterasu.gestao.util.TenantDataSourceManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    @Value("${app.db.hostname}")
    private String dbHostname;
    @Value("${app.db.port}")
    private String dbPort;
    @Value("${spring.datasource.username}")
    private String dbUsername;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final TenantDataSourceManager tenantDataSourceManager;

    public TenantRoutingDataSource(TenantDataSourceManager tenantDataSourceManager) {
        this.tenantDataSourceManager = tenantDataSourceManager;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant(); // Get the current tenant from context
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null || tenantId.equals(TenantContext.getMasterDb())) {
            return tenantDataSourceManager.getMasterDataSource();
        }

        return tenantDataSourceManager.getDataSource(tenantId) != null
                ? tenantDataSourceManager.getDataSource(tenantId)
                : createAndRegisterTenantDataSource(tenantId);
    }

    private synchronized DataSource createAndRegisterTenantDataSource(String tenantId) {
        DataSource existing = tenantDataSourceManager.getDataSource(tenantId);
        if (existing != null) {
            return existing;
        }

        String tenantUrl = String.format("jdbc:postgresql://%s:%s/%s", dbHostname, dbPort, tenantId);
        DataSource dataSource = DataSourceUtil.createDataSource(tenantUrl, dbUsername, dbPassword);

        try {
            org.flywaydb.core.Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration/tenant")
                    .baselineOnMigrate(true)
                    .load()
                    .migrate();
        } catch (Exception e) {
            System.err.println("Erro ao aplicar migracoes no tenant " + tenantId + ": " + e.getMessage());
        }

        tenantDataSourceManager.addDataSource(tenantId, dataSource);
        return dataSource;
    }
}
