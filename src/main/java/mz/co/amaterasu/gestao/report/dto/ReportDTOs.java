package mz.co.amaterasu.gestao.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.amaterasu.gestao.payment.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReportDTOs {

    private ReportDTOs() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodAmount {
        private PaymentMethod method;
        private String label;
        private BigDecimal amount;
    }

    /** Indicadores da seccao 8 da especificacao. */
    @Data
    public static class SummaryResponse {
        private LocalDate startDate;
        private LocalDate endDate;
        /** Entradas nao canceladas do periodo (atendimentos). */
        private long entriesCount;
        /** Viaturas distintas atendidas no periodo. */
        private long vehiclesCount;
        private BigDecimal servicesTotal;
        private BigDecimal receipts;
        private List<MethodAmount> receiptsByMethod = new ArrayList<>();
        private BigDecimal expenses;
        private List<MethodAmount> expensesByMethod = new ArrayList<>();
        /** Recebimentos liquidos menos despesas. Nao e lucro nem saldo de caixa. */
        private BigDecimal netMovement;
        /** Saldo actual das entradas com data de servico no periodo. */
        private BigDecimal receivablePeriod;
        /** Soma de todos os saldos pendentes, independentemente da data. */
        private BigDecimal totalDebt;
        private Map<String, Long> workStatusCounts = new LinkedHashMap<>();
    }

    @Data
    public static class StatementLine {
        private LocalDate date;
        /** SERVICE, OPENING, PAYMENT, REVERSAL ou REFUND. */
        private String type;
        private String reference;
        private String description;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal balance;
    }

    @Data
    public static class StatementResponse {
        private Long clientId;
        private String clientName;
        private String clientPhone;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal openingBalance;
        private BigDecimal totalDebits;
        private BigDecimal totalCredits;
        /** Saldo historico no fim do periodo. */
        private BigDecimal closingBalance;
        /** Divida actual, hoje; difere do saldo final quando o periodo nao chega a hoje. */
        private BigDecimal currentDebt;
        private List<StatementLine> lines = new ArrayList<>();
    }
}
