package mz.co.amaterasu.gestao.entry.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import mz.co.amaterasu.gestao.catalog.enums.PricingType;
import mz.co.amaterasu.gestao.client.enums.VehicleCategory;
import mz.co.amaterasu.gestao.entry.entity.Entry;
import mz.co.amaterasu.gestao.entry.entity.EntryLine;
import mz.co.amaterasu.gestao.entry.enums.EntryPaymentStatus;
import mz.co.amaterasu.gestao.entry.enums.WorkStatus;
import mz.co.amaterasu.gestao.payment.entity.PaymentAllocation;
import mz.co.amaterasu.gestao.payment.enums.MovementKind;
import mz.co.amaterasu.gestao.payment.enums.PaymentMethod;
import mz.co.amaterasu.gestao.util.MaputoTime;
import mz.co.amaterasu.gestao.util.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class EntryDTOs {

    private EntryDTOs() {
    }

    @Data
    public static class LineRequest {
        /** Linha existente a manter; sem id, a linha e nova e usa o preco actual do catalogo. */
        private Long id;
        @NotNull(message = "Seleccione o serviço")
        private Long serviceId;
        /** Quantidade ou horas; nos servicos por hora aceita decimais (ex.: 1,5). */
        private BigDecimal quantity;
        /** Obrigatorio nos servicos variaveis; nos restantes, diferente do catalogo exige adjust:prices. */
        private BigDecimal unitPrice;
        private String adjustReason;
    }

    @Data
    public static class EntryRequest {
        @NotNull(message = "Seleccione o cliente")
        private Long clientId;
        @NotNull(message = "Seleccione a viatura")
        private Long vehicleId;
        /** Data e hora locais de Maputo; omitida = agora. */
        private LocalDateTime entryAt;
        private String notes;
        @NotEmpty(message = "Adicione pelo menos um serviço")
        @Valid
        private List<LineRequest> lines = new ArrayList<>();
    }

    @Data
    public static class StatusRequest {
        @NotNull(message = "Indique o estado")
        private WorkStatus workStatus;
        /** Obrigatorio para entregar com saldo pendente a cliente sem conta corrente. */
        private String reason;
    }

    @Data
    public static class CancelRequest {
        @NotBlank(message = "Indique o motivo do cancelamento")
        private String reason;
    }

    @Data
    public static class OpeningBalanceRequest {
        @NotNull(message = "Seleccione o cliente")
        private Long clientId;
        @NotNull(message = "Indique o montante")
        @Positive(message = "O montante deve ser positivo")
        private BigDecimal amount;
        @NotNull(message = "Indique a data de corte")
        private LocalDate date;
        private String notes;
    }

    /** Filtros comuns a listagem e a exportacao. */
    public record EntryFilter(LocalDate startDate, LocalDate endDate, Long clientId, Long vehicleId,
                              WorkStatus workStatus, EntryPaymentStatus paymentStatus, Long serviceId,
                              String term) {
    }

    @Data
    public static class LineResponse {
        private Long id;
        private Long serviceId;
        private String serviceName;
        private PricingType pricingType;
        private VehicleCategory category;
        private BigDecimal quantity;
        private BigDecimal catalogPrice;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String adjustReason;

        static LineResponse from(EntryLine line) {
            LineResponse dto = new LineResponse();
            dto.setId(line.getId());
            dto.setServiceId(line.getService() == null ? null : line.getService().getId());
            dto.setServiceName(line.getServiceName());
            dto.setPricingType(line.getPricingType());
            dto.setCategory(line.getCategory());
            dto.setQuantity(line.getQuantity());
            dto.setCatalogPrice(line.getCatalogPrice());
            dto.setUnitPrice(line.getUnitPrice());
            dto.setSubtotal(line.getSubtotal());
            dto.setAdjustReason(line.getAdjustReason());
            return dto;
        }
    }

    @Data
    public static class AllocationResponse {
        private Long paymentId;
        private String paymentNumber;
        private LocalDate paymentDate;
        private PaymentMethod method;
        private MovementKind kind;
        private BigDecimal amount;

        public static AllocationResponse from(PaymentAllocation allocation) {
            AllocationResponse dto = new AllocationResponse();
            dto.setPaymentId(allocation.getPayment().getId());
            dto.setPaymentNumber(allocation.getPayment().getNumber());
            dto.setPaymentDate(allocation.getPayment().getPaymentDate());
            dto.setMethod(allocation.getPayment().getMethod());
            dto.setKind(allocation.getPayment().getKind());
            dto.setAmount(allocation.getAmount());
            return dto;
        }
    }

    @Data
    public static class EntryResponse {
        private Long id;
        private String number;
        private Long clientId;
        private String clientName;
        private boolean clientCreditAllowed;
        private Long vehicleId;
        private String vehiclePlate;
        private String vehicleDescription;
        private VehicleCategory vehicleCategory;
        private Instant entryAt;
        private LocalDate serviceDate;
        private WorkStatus workStatus;
        private BigDecimal total;
        private BigDecimal paid;
        private BigDecimal balance;
        private EntryPaymentStatus paymentStatus;
        private boolean openingBalance;
        private String responsible;
        private String notes;
        private Instant deliveredAt;
        private String deliveredBy;
        private String creditAuthorizedBy;
        private String creditReason;
        private Instant cancelledAt;
        private String cancelledBy;
        private String cancelReason;
        private LocalDateTime createdAt;
        private String createdBy;
        private List<LineResponse> lines = new ArrayList<>();
        private List<AllocationResponse> allocations = new ArrayList<>();

        public static EntryResponse from(Entry entry, BigDecimal paid, List<PaymentAllocation> allocations) {
            EntryResponse dto = new EntryResponse();
            dto.setId(entry.getId());
            dto.setNumber(entry.getNumber());
            dto.setClientId(entry.getClient().getId());
            dto.setClientName(entry.getClientName());
            dto.setClientCreditAllowed(entry.getClient().isCreditAllowed());
            dto.setVehicleId(entry.getVehicle() == null ? null : entry.getVehicle().getId());
            dto.setVehiclePlate(entry.getVehiclePlate());
            dto.setVehicleDescription(entry.getVehicleDescription());
            dto.setVehicleCategory(entry.getVehicleCategory());
            dto.setEntryAt(entry.getEntryAt());
            dto.setServiceDate(MaputoTime.toLocalDate(entry.getEntryAt()));
            dto.setWorkStatus(entry.getWorkStatus());
            dto.setTotal(entry.getTotal());
            dto.setPaid(Money.of(paid));
            boolean cancelled = entry.getWorkStatus() == WorkStatus.CANCELLED;
            BigDecimal balance = cancelled ? Money.ZERO : Money.of(entry.getTotal().subtract(paid));
            dto.setBalance(balance);
            dto.setPaymentStatus(paymentStatus(cancelled, paid, balance));
            dto.setOpeningBalance(entry.isOpeningBalance());
            dto.setResponsible(entry.getResponsible());
            dto.setNotes(entry.getNotes());
            dto.setDeliveredAt(entry.getDeliveredAt());
            dto.setDeliveredBy(entry.getDeliveredBy());
            dto.setCreditAuthorizedBy(entry.getCreditAuthorizedBy());
            dto.setCreditReason(entry.getCreditReason());
            dto.setCancelledAt(entry.getCancelledAt());
            dto.setCancelledBy(entry.getCancelledBy());
            dto.setCancelReason(entry.getCancelReason());
            dto.setCreatedAt(entry.getCreatedAt());
            dto.setCreatedBy(entry.getCreatedBy());
            entry.getLines().forEach(line -> dto.getLines().add(LineResponse.from(line)));
            allocations.forEach(allocation -> dto.getAllocations().add(AllocationResponse.from(allocation)));
            return dto;
        }

        /** Mesma regra da vista vw_entries. */
        private static EntryPaymentStatus paymentStatus(boolean cancelled, BigDecimal paid, BigDecimal balance) {
            if (cancelled) {
                return EntryPaymentStatus.CANCELLED;
            }
            if (balance.signum() <= 0) {
                return EntryPaymentStatus.PAID;
            }
            return paid.signum() > 0 ? EntryPaymentStatus.PARTIAL : EntryPaymentStatus.UNPAID;
        }
    }
}
