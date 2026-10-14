package mz.co.amaterasu.gestao.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import mz.co.amaterasu.gestao.expense.entity.Expense;
import mz.co.amaterasu.gestao.payment.enums.MovementKind;
import mz.co.amaterasu.gestao.payment.enums.MovementStatus;
import mz.co.amaterasu.gestao.payment.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ExpenseDTOs {

    private ExpenseDTOs() {
    }

    @Data
    public static class ExpenseRequest {
        @NotBlank(message = "A descrição é obrigatória")
        private String description;
        @NotNull(message = "Seleccione a categoria")
        private Long categoryId;
        @NotNull(message = "Indique o valor")
        @Positive(message = "O valor deve ser positivo")
        private BigDecimal amount;
        /** Omitida = hoje (Maputo). */
        private LocalDate expenseDate;
        @NotNull(message = "Seleccione o método de pagamento")
        private PaymentMethod method;
        private String notes;
    }

    @Data
    public static class ReversalRequest {
        @NotBlank(message = "Indique o motivo da correcção")
        private String reason;
    }

    @Data
    public static class CategoryRequest {
        @NotBlank(message = "O nome é obrigatório")
        private String name;
        private Boolean active;
    }

    public record ExpenseFilter(LocalDate startDate, LocalDate endDate, Long categoryId, PaymentMethod method,
                                String term) {
    }

    @Data
    public static class ExpenseResponse {
        private Long id;
        private String number;
        private MovementKind kind;
        private String description;
        private Long categoryId;
        private String categoryName;
        private LocalDate expenseDate;
        private BigDecimal amount;
        private PaymentMethod method;
        private String notes;
        private MovementStatus status;
        private Long reversalOfId;
        private String reversalOfNumber;
        private String reason;
        private LocalDateTime createdAt;
        private String createdBy;

        public static ExpenseResponse from(Expense expense) {
            ExpenseResponse dto = new ExpenseResponse();
            dto.setId(expense.getId());
            dto.setNumber(expense.getNumber());
            dto.setKind(expense.getKind());
            dto.setDescription(expense.getDescription());
            dto.setCategoryId(expense.getCategory().getId());
            dto.setCategoryName(expense.getCategory().getName());
            dto.setExpenseDate(expense.getExpenseDate());
            dto.setAmount(expense.getAmount());
            dto.setMethod(expense.getMethod());
            dto.setNotes(expense.getNotes());
            dto.setStatus(expense.getStatus());
            if (expense.getReversalOf() != null) {
                dto.setReversalOfId(expense.getReversalOf().getId());
                dto.setReversalOfNumber(expense.getReversalOf().getNumber());
            }
            dto.setReason(expense.getReason());
            dto.setCreatedAt(expense.getCreatedAt());
            dto.setCreatedBy(expense.getCreatedBy());
            return dto;
        }
    }
}
