package mz.co.amaterasu.gestao.base.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.amaterasu.gestao.base.entity.Tenant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO {

    private Long id;
    private String tenantId;
    private String tenantName;
    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;
    private boolean isActive;


    public TenantDTO(Tenant tenant) {
        this.id = tenant.getId();
        this.tenantId = tenant.getSlug();
        this.tenantName = tenant.getTenantName();
        this.databaseUrl = tenant.getDatabaseUrl();
        this.databaseUsername = tenant.getDatabaseUsername();
        this.databasePassword = tenant.getDatabasePassword();
        this.isActive = tenant.isActive();
    }
}
