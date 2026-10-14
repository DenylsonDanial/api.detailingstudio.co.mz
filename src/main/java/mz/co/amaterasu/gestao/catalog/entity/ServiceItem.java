package mz.co.amaterasu.gestao.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.catalog.enums.PricingType;
import mz.co.amaterasu.gestao.client.enums.VehicleCategory;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.util.Auditable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Servico do catalogo. Chama-se ServiceItem para nao colidir com a anotacao @Service. */
@Entity
@Table(name = "tb_services")
@Getter
@Setter
@NoArgsConstructor
public class ServiceItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false)
    private PricingType pricingType;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    private VehicleCategory category;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicePrice> prices = new ArrayList<>();

    /**
     * Preco de catalogo aplicavel a uma categoria de viatura. Devolve nulo nos
     * servicos de preco variavel, em que o valor e introduzido no trabalho.
     */
    public BigDecimal priceFor(VehicleCategory vehicleCategory) {
        if (category != null && vehicleCategory != null && category != vehicleCategory) {
            throw new ApiRequestException(String.format("O serviço \"%s\" só se aplica à categoria %s.",
                    name, category.getLabel()));
        }
        return switch (pricingType) {
            case VARIABLE -> null;
            case FIXED, HOURLY -> {
                if (basePrice == null) {
                    throw new ApiRequestException(String.format("O serviço \"%s\" não tem preço definido.", name));
                }
                yield basePrice;
            }
            case BY_CATEGORY -> prices.stream()
                    .filter(price -> price.getCategory() == vehicleCategory)
                    .map(ServicePrice::getPrice)
                    .findFirst()
                    .orElseThrow(() -> new ApiRequestException(String.format(
                            "O serviço \"%s\" não tem preço para a categoria %s.", name,
                            vehicleCategory == null ? "indicada" : vehicleCategory.getLabel())));
        };
    }
}
