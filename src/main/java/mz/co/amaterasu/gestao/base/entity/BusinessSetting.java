package mz.co.amaterasu.gestao.base.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@ToString
@Table(name = "tb_business_settings")
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    @Column(unique = true)
    private Long id;
    @Getter @Setter @Column(name = "business_name")
    private String businessName;
    @Getter @Setter
    private Long nuit;
    @Getter @Setter
    private String email;
    @Getter @Setter
    private String address;
    @Getter @Setter
    private String website;
    @Getter @Setter @Column(columnDefinition = "TEXT")
    private String logo;
    @Getter @Setter
    private String phone;
    @Getter @Setter @Column(name = "alt_phone")
    private String altPhone;
    @Getter @Setter
    private String province;
    @Getter @Setter
    private String city;
    @Getter @Setter @Column(name = "activity_sector")
    private String activitySector;
    @Getter @Setter @Column(name = "db_tenant_name")
    private String dbTenantName;
    // Sem cascade: as moedas sao dados de referencia carregados pela migracao e
    // nunca devem ser criadas ou apagadas ao gravar as definicoes.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    @Getter @Setter
    private Currency currency;
    @Getter @Setter @Column(name = "default_tax_rate", precision = 5, scale = 2)
    private BigDecimal defaultTaxRate;
    @Getter @Setter @Column(columnDefinition = "boolean default true", name = "is_active")
    private boolean isActive;
    @Getter @Setter @Column(name = "activity_start")
    private Date activityStart;
    @Getter @Setter @Column(name = "created_by")
    private String createdBy;
    @Getter @Setter @Column(name = "updated_by")
    private String updatedBy;
    @Getter @Setter @Column(name = "created_at")
    @Temporal(TemporalType.DATE)
    private Date createdAt;
    @Getter @Setter @Column(name = "updated_at")
    @Temporal(TemporalType.DATE)
    private Date updatedAt;

}
