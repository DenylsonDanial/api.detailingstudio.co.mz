package mz.co.amaterasu.gestao.entry.repository;

import jakarta.persistence.LockModeType;
import mz.co.amaterasu.gestao.entry.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Entry e where e.id = :id")
    Optional<Entry> findByIdForUpdate(@Param("id") Long id);

    /** SELECT ... FOR UPDATE, por ordem de antiguidade (a ordem da distribuicao automatica). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Entry e where e.id in :ids order by e.entryAt asc, e.id asc")
    List<Entry> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
