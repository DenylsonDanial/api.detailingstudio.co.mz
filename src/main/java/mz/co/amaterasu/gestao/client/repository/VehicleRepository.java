package mz.co.amaterasu.gestao.client.repository;

import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.client.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findFirstByPlateNormalizedAndStatus(String plateNormalized, RecordStatus status);

    List<Vehicle> findByClientIdOrderByStatusAscPlateAsc(Long clientId);

    List<Vehicle> findByClientIdInAndStatus(Collection<Long> clientIds, RecordStatus status);

    /** clientId = 0 significa "todos os clientes". */
    @Query(value = "select v from Vehicle v join fetch v.client c where v.status in :statuses " +
            "and (:clientId = 0 or c.id = :clientId) " +
            "and (v.plateNormalized like :plate or lower(coalesce(v.brand, '')) like :term " +
            "or lower(coalesce(v.model, '')) like :term or lower(c.name) like :term)",
            countQuery = "select count(v) from Vehicle v join v.client c where v.status in :statuses " +
                    "and (:clientId = 0 or c.id = :clientId) " +
                    "and (v.plateNormalized like :plate or lower(coalesce(v.brand, '')) like :term " +
                    "or lower(coalesce(v.model, '')) like :term or lower(c.name) like :term)")
    Page<Vehicle> search(@Param("statuses") Collection<RecordStatus> statuses,
                         @Param("clientId") long clientId,
                         @Param("term") String term,
                         @Param("plate") String plate,
                         Pageable pageable);
}
