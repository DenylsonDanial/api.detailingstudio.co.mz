package mz.co.amaterasu.gestao.client.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.entity.ChangeHistory;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.base.service.ChangeHistoryService;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.ClientRequest;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.ClientResponse;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.CreditRequest;
import mz.co.amaterasu.gestao.client.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;
    private final ChangeHistoryService changeHistoryService;

    /** Pesquisa por nome, contacto ou matricula. */
    @PreAuthorize("hasAuthority('read:clients')")
    @GetMapping("/list")
    public Page<ClientResponse> list(Pageable pageable,
                                     @RequestParam(required = false) RecordStatus status,
                                     @RequestParam(required = false) String term) {
        return clientService.findAll(pageable, status, term);
    }

    @PreAuthorize("hasAuthority('read:clients')")
    @GetMapping("/findById/{id}")
    public ResponseEntity<ClientResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.findById(id));
    }

    @PreAuthorize("hasAuthority('read:clients')")
    @GetMapping("/duplicates")
    public ResponseEntity<List<ClientResponse>> duplicates(@RequestParam String phone,
                                                           @RequestParam(required = false) Long excludeId) {
        return ResponseEntity.ok(clientService.findPossibleDuplicates(phone, excludeId));
    }

    @PreAuthorize("hasAuthority('read:clients')")
    @GetMapping("/history/{id}")
    public ResponseEntity<List<ChangeHistory>> history(@PathVariable Long id) {
        return ResponseEntity.ok(changeHistoryService.findFor(ChangeHistoryService.CLIENT, id));
    }

    @PreAuthorize("hasAuthority('create:clients')")
    @PostMapping("/insert")
    public ResponseEntity<ClientResponse> insert(@RequestBody @Valid ClientRequest request) {
        return ResponseEntity.ok(clientService.create(request));
    }

    @PreAuthorize("hasAuthority('update:clients')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id, @RequestBody @Valid ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @PreAuthorize("hasAuthority('manage:credit')")
    @PutMapping("/credit/{id}")
    public ResponseEntity<ClientResponse> credit(@PathVariable Long id, @RequestBody @Valid CreditRequest request) {
        return ResponseEntity.ok(clientService.updateCredit(id, request));
    }

    /** Clientes nao se eliminam: arquivam-se, preservando todo o historico. */
    @PreAuthorize("hasAuthority('delete:clients')")
    @PutMapping("/archive/{id}")
    public ResponseEntity<ClientResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.setStatus(id, RecordStatus.ARCHIVED));
    }

    @PreAuthorize("hasAuthority('delete:clients')")
    @PutMapping("/unarchive/{id}")
    public ResponseEntity<ClientResponse> unarchive(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.setStatus(id, RecordStatus.ACTIVE));
    }
}
