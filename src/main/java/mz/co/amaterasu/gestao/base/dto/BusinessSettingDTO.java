package mz.co.amaterasu.gestao.base.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import mz.co.amaterasu.gestao.base.entity.Currency;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BusinessSettingDTO {

    private Long id;
    private String businessName;
    private Long nuit;
    private String email;
    private String address;
    private String website;
    private String logo;
    private String phone;
    private String altPhone;
    private String province;
    private String city;
    private String activitySector;
    private String dbTenantName;
    /** Leitura: moeda completa. Escrita: usar currencyId. */
    private Currency currency;
    private Long currencyId;
    private BigDecimal defaultTaxRate;
    private boolean isActive;
    private Date activityStart;
    private String createdBy;
    private String updatedBy;
    private Date createdAt;
    private Date updatedAt;
}
