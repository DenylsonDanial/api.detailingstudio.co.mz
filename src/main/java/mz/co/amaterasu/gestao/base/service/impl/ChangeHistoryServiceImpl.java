package mz.co.amaterasu.gestao.base.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.entity.ChangeHistory;
import mz.co.amaterasu.gestao.base.repository.ChangeHistoryRepository;
import mz.co.amaterasu.gestao.base.service.ChangeHistoryService;
import mz.co.amaterasu.gestao.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeHistoryServiceImpl implements ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;

    @Override
    @Transactional
    public void record(String entityType, Long entityId, String action, String reason, Object oldValue, Object newValue) {
        ChangeHistory history = new ChangeHistory();
        history.setEntityType(entityType);
        history.setEntityId(entityId);
        history.setAction(action);
        history.setReason(reason);
        history.setOldValue(oldValue == null ? null : String.valueOf(oldValue));
        history.setNewValue(newValue == null ? null : String.valueOf(newValue));
        history.setChangedBy(SecurityUtils.currentUsername());
        history.setChangedAt(Instant.now());
        changeHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChangeHistory> findFor(String entityType, Long entityId) {
        return changeHistoryRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId);
    }
}
