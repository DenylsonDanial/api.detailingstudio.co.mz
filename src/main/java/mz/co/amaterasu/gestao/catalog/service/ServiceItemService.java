package mz.co.amaterasu.gestao.catalog.service;

import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.catalog.dto.ServiceItemDTOs.ServiceItemRequest;
import mz.co.amaterasu.gestao.catalog.dto.ServiceItemDTOs.ServiceItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceItemService {

    Page<ServiceItemResponse> findAll(Pageable pageable, RecordStatus status, String term);

    ServiceItemResponse findById(Long id);

    ServiceItemResponse create(ServiceItemRequest request);

    ServiceItemResponse update(Long id, ServiceItemRequest request);

    ServiceItemResponse setStatus(Long id, RecordStatus status, String reason);
}
