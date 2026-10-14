package mz.co.amaterasu.gestao.base.service;

import mz.co.amaterasu.gestao.base.entity.ChangeHistory;

import java.util.List;

public interface ChangeHistoryService {

    String CLIENT = "CLIENT";
    String VEHICLE = "VEHICLE";
    String SERVICE = "SERVICE";
    String ENTRY = "ENTRY";
    String PAYMENT = "PAYMENT";
    String EXPENSE = "EXPENSE";

    void record(String entityType, Long entityId, String action, String reason, Object oldValue, Object newValue);

    List<ChangeHistory> findFor(String entityType, Long entityId);
}
