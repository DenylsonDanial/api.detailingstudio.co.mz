package mz.co.amaterasu.gestao.payment.repository;

import jakarta.persistence.LockModeType;
import mz.co.amaterasu.gestao.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByReversalOfId(Long reversalOfId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") Long id);

    /** Recebido liquido do cliente (os estornos tem sinal negativo). */
    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.client.id = :clientId")
    BigDecimal totalReceivedByClient(@Param("clientId") Long clientId);
}
