package mz.co.amaterasu.gestao.base.config;

import jakarta.annotation.PostConstruct;
import mz.co.amaterasu.gestao.util.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TenantContextInitializer {

    @Value("${app.db.master.name}")
    private String masterDbName;

    @PostConstruct
    void init() {
        TenantContext.configureMasterDb(masterDbName);
    }
}
