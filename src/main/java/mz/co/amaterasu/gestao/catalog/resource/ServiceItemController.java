package mz.co.amaterasu.gestao.catalog.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.catalog.dto.ServiceItemDTOs.ServiceItemRequest;
import mz.co.amaterasu.gestao.catalog.dto.ServiceItemDTOs.ServiceItemResponse;
import mz.co.amaterasu.gestao.catalog.service.ServiceItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Catalogo de servicos e precos. So o gestor altera (especificacao, seccao 6). */
@RestController
@RequiredArgsConstructor
@RequestMapping("/services")
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    @PreAuthorize("hasAuthority('read:services')")
    @GetMapping("/list")
    public Page<ServiceItemResponse> list(Pageable pageable,
                                          @RequestParam(required = false) RecordStatus status,
                                          @RequestParam(required = false) String term) {
        return serviceItemService.findAll(pageable, status, term);
    }

    @PreAuthorize("hasAuthority('read:services')")
    @GetMapping("/findById/{id}")
    public ResponseEntity<ServiceItemResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceItemService.findById(id));
    }

    @PreAuthorize("hasAuthority('create:services')")
    @PostMapping("/insert")
    public ResponseEntity<ServiceItemResponse> insert(@RequestBody @Valid ServiceItemRequest request) {
        return ResponseEntity.ok(serviceItemService.create(request));
    }

    @PreAuthorize("hasAuthority('update:services')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ServiceItemResponse> update(@PathVariable Long id,
                                                      @RequestBody @Valid ServiceItemRequest request) {
        return ResponseEntity.ok(serviceItemService.update(id, request));
    }

    /** Servicos com historico nao se eliminam: arquivam-se. */
    @PreAuthorize("hasAuthority('delete:services')")
    @PutMapping("/archive/{id}")
    public ResponseEntity<ServiceItemResponse> archive(@PathVariable Long id,
                                                       @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(serviceItemService.setStatus(id, RecordStatus.ARCHIVED, reason));
    }

    @PreAuthorize("hasAuthority('update:services')")
    @PutMapping("/unarchive/{id}")
    public ResponseEntity<ServiceItemResponse> unarchive(@PathVariable Long id) {
        return ResponseEntity.ok(serviceItemService.setStatus(id, RecordStatus.ACTIVE, null));
    }
}
