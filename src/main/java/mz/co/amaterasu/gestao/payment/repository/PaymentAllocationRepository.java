package mz.co.amaterasu.gestao.payment.repository;

import mz.co.amaterasu.gestao.payment.entity.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Repository
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {

    @Query("select coalesce(sum(a.amount), 0) from PaymentAllocation a where a.entry.id = :entryId")
    BigDecimal paidByEntry(@Param("entryId") Long entryId);

    @Query("select a from PaymentAllocation a join fetch a.payment p where a.entry.id = :entryId order by p.paymentDate, a.id")
    List<PaymentAllocation> findByEntry(@Param("entryId") Long entryId);

    @Query("select a.entry.id, coalesce(sum(a.amount), 0) from PaymentAllocation a where a.entry.id in :entryIds group by a.entry.id")
    List<Object[]> paidByEntries(@Param("entryIds") Collection<Long> entryIds);

    @Query("select a from PaymentAllocation a join fetch a.entry where a.payment.id = :paymentId order by a.id")
    List<PaymentAllocation> findByPayment(@Param("paymentId") Long paymentId);
}
