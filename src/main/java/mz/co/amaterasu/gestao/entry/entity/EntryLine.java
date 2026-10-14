package mz.co.amaterasu.gestao.entry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.catalog.entity.ServiceItem;
import mz.co.amaterasu.gestao.catalog.enums.PricingType;
import mz.co.amaterasu.gestao.client.enums.VehicleCategory;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_entry_lines")
@Getter
@Setter
@NoArgsConstructor
public class EntryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_id", nullable = false)
    private Entry entry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceItem service;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false)
    private PricingType pricingType;

    @Enumerated(EnumType.STRING)
    private VehicleCategory category;

    @Column(nullable = false)
    private BigDecimal quantity;

    /** Preco de catalogo no momento do registo; nulo nos servicos variaveis. */
    @Column(name = "catalog_price")
    private BigDecimal catalogPrice;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "adjust_reason")
    private String adjustReason;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
