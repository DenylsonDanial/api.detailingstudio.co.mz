package mz.co.amaterasu.gestao.client.repository;

import jakarta.persistence.LockModeType;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Pesquisa por nome, contacto ou matricula. Os padroes chegam sempre
     * preenchidos ("%" quando nao ha termo) para nao depender de parametros nulos.
     */
    @Query(value = "select c from Client c where c.status in :statuses and (lower(c.name) like :term " +
            "or coalesce(c.phone, '') like :term or exists (select 1 from Vehicle v " +
            "where v.client = c and v.plateNormalized like :plate))")
    Page<Client> search(@Param("statuses") Collection<RecordStatus> statuses,
                        @Param("term") String term,
                        @Param("plate") String plate,
                        Pageable pageable);

    @Query(value = "select * from tb_clients c where c.id <> :excludeId " +
            "and regexp_replace(coalesce(c.phone, ''), '[^0-9]', '', 'g') = :digits", nativeQuery = true)
    List<Client> findByPhoneDigits(@Param("digits") String digits, @Param("excludeId") Long excludeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Client c where c.id = :id")
    Optional<Client> findByIdForUpdate(@Param("id") Long id);
}
