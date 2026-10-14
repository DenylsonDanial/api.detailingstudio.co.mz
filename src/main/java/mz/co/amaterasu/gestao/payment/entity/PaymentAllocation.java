package mz.co.amaterasu.gestao.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.entry.entity.Entry;

import java.math.BigDecimal;

/** Parte de um pagamento (ou estorno) atribuida a uma entrada. */
@Entity
@Table(name = "tb_payment_allocations")
@Getter
@Setter
@NoArgsConstructor
public class PaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, updatable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_id", nullable = false, updatable = false)
    private Entry entry;

    @Column(nullable = false, updatable = false)
    private BigDecimal amount;
}
