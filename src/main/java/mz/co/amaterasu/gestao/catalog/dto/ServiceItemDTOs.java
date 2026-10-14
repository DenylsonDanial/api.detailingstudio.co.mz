package mz.co.amaterasu.gestao.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.catalog.entity.ServiceItem;
import mz.co.amaterasu.gestao.catalog.enums.PricingType;
import mz.co.amaterasu.gestao.client.enums.VehicleCategory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ServiceItemDTOs {

    private ServiceItemDTOs() {
    }

    @Data
    public static class CategoryPrice {
        @NotNull(message = "A categoria é obrigatória")
        private VehicleCategory category;
        @NotNull(message = "O preço é obrigatório")
        @PositiveOrZero(message = "O preço não pode ser negativo")
        private BigDecimal price;
    }

    @Data
    public static class ServiceItemRequest {
        private String code;
        @NotBlank(message = "O nome é obrigatório")
        private String name;
        @NotNull(message = "O tipo de preço é obrigatório")
        private PricingType pricingType;
        @PositiveOrZero(message = "O preço não pode ser negativo")
        private BigDecimal basePrice;
        private VehicleCategory category;
        private Integer sortOrder;
        @Valid
        private List<CategoryPrice> prices = new ArrayList<>();
        /** Obrigatorio ao alterar um servico existente. */
        private String reason;
    }

    @Data
    public static class ServiceItemResponse {
        private Long id;
        private String code;
        private String name;
        private PricingType pricingType;
        private BigDecimal basePrice;
        private VehicleCategory category;
        private int sortOrder;
        private RecordStatus status;
        private List<CategoryPrice> prices = new ArrayList<>();

        public static ServiceItemResponse from(ServiceItem service) {
            ServiceItemResponse dto = new ServiceItemResponse();
            dto.setId(service.getId());
            dto.setCode(service.getCode());
            dto.setName(service.getName());
            dto.setPricingType(service.getPricingType());
            dto.setBasePrice(service.getBasePrice());
            dto.setCategory(service.getCategory());
            dto.setSortOrder(service.getSortOrder());
            dto.setStatus(service.getStatus());
            service.getPrices().stream()
                    .sorted(Comparator.comparing(price -> price.getCategory().ordinal()))
                    .forEach(price -> {
                        CategoryPrice item = new CategoryPrice();
                        item.setCategory(price.getCategory());
                        item.setPrice(price.getPrice());
                        dto.getPrices().add(item);
                    });
            return dto;
        }

        /** Representacao textual dos precos, usada no historico de alteracoes. */
        public String priceSummary() {
            StringBuilder sb = new StringBuilder(pricingType.name());
            if (basePrice != null) {
                sb.append(" base=").append(basePrice.toPlainString());
            }
            prices.forEach(price -> sb.append(' ').append(price.getCategory()).append('=')
                    .append(price.getPrice().toPlainString()));
            return sb.toString();
        }
    }
}
