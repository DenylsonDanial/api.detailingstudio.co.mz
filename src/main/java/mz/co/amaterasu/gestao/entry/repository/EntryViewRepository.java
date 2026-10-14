package mz.co.amaterasu.gestao.entry.repository;

import mz.co.amaterasu.gestao.entry.entity.EntryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Repository
public interface EntryViewRepository extends JpaRepository<EntryView, Long>, JpaSpecificationExecutor<EntryView> {

    /** Entradas com saldo, das mais antigas para as mais recentes (ordem da distribuicao automatica). */
    @Query("select e from EntryView e where e.clientId = :clientId and e.balance > 0 order by e.entryAt asc, e.id asc")
    List<EntryView> findOpenByClient(@Param("clientId") Long clientId);

    @Query("select coalesce(sum(e.balance), 0) from EntryView e where e.clientId = :clientId")
    BigDecimal debtByClient(@Param("clientId") Long clientId);

    @Query("select e.clientId, coalesce(sum(e.balance), 0) from EntryView e where e.clientId in :clientIds group by e.clientId")
    List<Object[]> debtByClients(@Param("clientIds") Collection<Long> clientIds);

    @Query("select coalesce(sum(e.total), 0) from EntryView e " +
            "where e.clientId = :clientId and e.paymentStatus <> mz.co.amaterasu.gestao.entry.enums.EntryPaymentStatus.CANCELLED")
    BigDecimal totalServicesByClient(@Param("clientId") Long clientId);
}
