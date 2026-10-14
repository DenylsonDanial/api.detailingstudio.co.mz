package mz.co.amaterasu.gestao.base.repository;

import jakarta.persistence.LockModeType;
import mz.co.amaterasu.gestao.base.entity.DocumentSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DocumentSequence> findByDocumentTypeAndYear(String documentType, Integer year);

    Optional<DocumentSequence> findFirstByDocumentTypeOrderByYearDesc(String documentType);
}
