package mz.co.amaterasu.gestao.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

@Component
public class TenantDataSourceManager {
    private final DataSource masterDataSource; // DataSource for the master database
    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    public TenantDataSourceManager(DataSource masterDataSource) {
        this.masterDataSource = masterDataSource;
    }

    public void addDataSource(String tenantId, DataSource dataSource) {
        tenantDataSources.put(tenantId, dataSource);
    }

    public DataSource getDataSource(String tenantId) {
        return tenantDataSources.get(tenantId);
    }

    public DataSource getMasterDataSource() {
        return masterDataSource;
    }
}
