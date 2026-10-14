package mz.co.amaterasu.gestao.base.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_tenants")
@Data
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador da base de dados (coluna tenant_id). Nao pode chamar-se
     * tenantId: o Hibernate 6 trata esse nome como discriminador de tenant da
     * sessao e, no setup (JWT sem empresa), grava null.
     */
    @Column(nullable = false, unique = true, name = "tenant_id")
    private String slug;

    @Column(nullable = false, name = "tenant_name")
    private String tenantName;

    @Column(name = "database_url")
    private String databaseUrl;

    @Column(name = "database_username")
    private String databaseUsername;
    @Column(name = "database_password")
    private String databasePassword;
    @Getter
    @Setter
    @Column(columnDefinition = "boolean default true", name = "is_active")
    private boolean isActive;
}
