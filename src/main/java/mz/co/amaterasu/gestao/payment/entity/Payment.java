package mz.co.amaterasu.gestao.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.client.entity.Client;
import mz.co.amaterasu.gestao.payment.enums.MovementKind;
import mz.co.amaterasu.gestao.payment.enums.MovementStatus;
import mz.co.amaterasu.gestao.payment.enums.PaymentMethod;
import mz.co.amaterasu.gestao.util.Auditable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, updatable = false)
    private Client client;

    @Column(name = "client_name", nullable = false, updatable = false)
    private String clientName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MovementKind kind;

    @Column(name = "payment_date", nullable = false, updatable = false)
    private LocalDate paymentDate;

    /** Positivo nos pagamentos, negativo nos estornos. */
    @Column(nullable = false, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PaymentMethod method;

    private String reference;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementStatus status = MovementStatus.VALID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_of_id", updatable = false)
    private Payment reversalOf;

    @Column(updatable = false)
    private String reason;

    @Column(nullable = false, updatable = false)
    private boolean refund;

    @Column(name = "idempotency_key", unique = true, updatable = false)
    private String idempotencyKey;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.PERSIST)
    @OrderBy("id ASC")
    private List<PaymentAllocation> allocations = new ArrayList<>();
}
