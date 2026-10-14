package mz.co.amaterasu.gestao.expense.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.payment.enums.MovementKind;
import mz.co.amaterasu.gestao.payment.enums.MovementStatus;
import mz.co.amaterasu.gestao.payment.enums.PaymentMethod;
import mz.co.amaterasu.gestao.util.Auditable;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Saida efectivamente paga. Correccoes sao estornos de sinal negativo; nada se apaga. */
@Entity
@Table(name = "tb_expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MovementKind kind;

    @Column(nullable = false, updatable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, updatable = false)
    private ExpenseCategory category;

    @Column(name = "expense_date", nullable = false, updatable = false)
    private LocalDate expenseDate;

    @Column(nullable = false, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PaymentMethod method;

    @Column(updatable = false)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementStatus status = MovementStatus.VALID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_of_id", updatable = false)
    private Expense reversalOf;

    @Column(updatable = false)
    private String reason;
}
