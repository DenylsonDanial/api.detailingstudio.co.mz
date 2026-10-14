package mz.co.amaterasu.gestao.base.repository;

import mz.co.amaterasu.gestao.base.entity.ChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeHistoryRepository extends JpaRepository<ChangeHistory, Long> {

    List<ChangeHistory> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, Long entityId);
}
