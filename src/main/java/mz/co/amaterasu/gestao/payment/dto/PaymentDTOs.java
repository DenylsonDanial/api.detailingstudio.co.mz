package mz.co.amaterasu.gestao.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import mz.co.amaterasu.gestao.payment.entity.Payment;
import mz.co.amaterasu.gestao.payment.entity.PaymentAllocation;
import mz.co.amaterasu.gestao.payment.enums.MovementKind;
import mz.co.amaterasu.gestao.payment.enums.MovementStatus;
import mz.co.amaterasu.gestao.payment.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PaymentDTOs {

    private PaymentDTOs() {
    }

    @Data
    public static class AllocationRequest {
        @NotNull(message = "Seleccione a entrada")
        private Long entryId;
        @NotNull(message = "Indique o montante a afectar")
        @Positive(message = "O montante afectado deve ser positivo")
        private BigDecimal amount;
    }

    @Data
    public static class PaymentRequest {
        @NotNull(message = "Seleccione o cliente")
        private Long clientId;
        /** Omitida = hoje (Maputo). */
        private LocalDate paymentDate;
        @NotNull(message = "Indique o montante")
        @Positive(message = "O montante deve ser positivo")
        private BigDecimal amount;
        @NotNull(message = "Seleccione o método de pagamento")
        private PaymentMethod method;
        private String reference;
        private String notes;
        /** Gerada pelo formulario; um segundo envio com a mesma chave devolve o pagamento ja gravado. */
        @Size(max = 64)
        private String idempotencyKey;
        /** Vazio = distribuicao automatica pelas entradas mais antigas. */
        @Valid
        private List<AllocationRequest> allocations = new ArrayList<>();
    }

    @Data
    public static class ReversalRequest {
        @NotBlank(message = "Indique o motivo do estorno")
        private String reason;
        /** Verdadeiro quando o dinheiro e efectivamente devolvido ao cliente. */
        private boolean refund;
        private LocalDate refundDate;
        private PaymentMethod refundMethod;
    }

    public record PaymentFilter(LocalDate startDate, LocalDate endDate, Long clientId, PaymentMethod method,
                                MovementKind kind, String term) {
    }

    @Data
    public static class AllocationPreview {
        private Long entryId;
        private String entryNumber;
        private Instant entryAt;
        private String vehiclePlate;
        private BigDecimal total;
        private BigDecimal balance;
        private BigDecimal amount;
    }

    @Data
    public static class AllocationLine {
        private Long entryId;
        private String entryNumber;
        private String vehiclePlate;
        private BigDecimal amount;

        static AllocationLine from(PaymentAllocation allocation) {
            AllocationLine dto = new AllocationLine();
            dto.setEntryId(allocation.getEntry().getId());
            dto.setEntryNumber(allocation.getEntry().getNumber());
            dto.setVehiclePlate(allocation.getEntry().getVehiclePlate());
            dto.setAmount(allocation.getAmount());
            return dto;
        }
    }

    @Data
    public static class PaymentResponse {
        private Long id;
        private String number;
        private Long clientId;
        private String clientName;
        private MovementKind kind;
        private LocalDate paymentDate;
        private BigDecimal amount;
        private PaymentMethod method;
        private String reference;
        private String notes;
        private MovementStatus status;
        private Long reversalOfId;
        private String reversalOfNumber;
        private String reason;
        private boolean refund;
        private LocalDateTime createdAt;
        private String createdBy;
        private List<AllocationLine> allocations = new ArrayList<>();

        public static PaymentResponse from(Payment payment) {
            PaymentResponse dto = new PaymentResponse();
            dto.setId(payment.getId());
            dto.setNumber(payment.getNumber());
            dto.setClientId(payment.getClient().getId());
            dto.setClientName(payment.getClientName());
            dto.setKind(payment.getKind());
            dto.setPaymentDate(payment.getPaymentDate());
            dto.setAmount(payment.getAmount());
            dto.setMethod(payment.getMethod());
            dto.setReference(payment.getReference());
            dto.setNotes(payment.getNotes());
            dto.setStatus(payment.getStatus());
            if (payment.getReversalOf() != null) {
                dto.setReversalOfId(payment.getReversalOf().getId());
                dto.setReversalOfNumber(payment.getReversalOf().getNumber());
            }
            dto.setReason(payment.getReason());
            dto.setRefund(payment.isRefund());
            dto.setCreatedAt(payment.getCreatedAt());
            dto.setCreatedBy(payment.getCreatedBy());
            return dto;
        }

        public static PaymentResponse from(Payment payment, List<PaymentAllocation> allocations) {
            PaymentResponse dto = from(payment);
            allocations.forEach(allocation -> dto.getAllocations().add(AllocationLine.from(allocation)));
            return dto;
        }
    }
}
