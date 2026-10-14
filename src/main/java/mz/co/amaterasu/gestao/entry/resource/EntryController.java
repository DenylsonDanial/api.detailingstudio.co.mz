package mz.co.amaterasu.gestao.entry.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.entity.ChangeHistory;
import mz.co.amaterasu.gestao.base.service.ChangeHistoryService;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.CancelRequest;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryFilter;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryRequest;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryResponse;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.OpeningBalanceRequest;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.StatusRequest;
import mz.co.amaterasu.gestao.entry.entity.EntryView;
import mz.co.amaterasu.gestao.entry.enums.EntryPaymentStatus;
import mz.co.amaterasu.gestao.entry.enums.WorkStatus;
import mz.co.amaterasu.gestao.entry.service.EntryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/entries")
public class EntryController {

    private final EntryService entryService;
    private final ChangeHistoryService changeHistoryService;

    @PreAuthorize("hasAuthority('read:entries')")
    @GetMapping("/list")
    public Page<EntryView> list(Pageable pageable,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                @RequestParam(required = false) Long clientId,
                                @RequestParam(required = false) Long vehicleId,
                                @RequestParam(required = false) WorkStatus workStatus,
                                @RequestParam(required = false) EntryPaymentStatus paymentStatus,
                                @RequestParam(required = false) Long serviceId,
                                @RequestParam(required = false) String term) {
        return entryService.findAll(new EntryFilter(startDate, endDate, clientId, vehicleId, workStatus,
                paymentStatus, serviceId, term), pageable);
    }

    /** Entradas do cliente com saldo, das mais antigas para as mais recentes. */
    @PreAuthorize("hasAuthority('read:entries')")
    @GetMapping("/open/{clientId}")
    public ResponseEntity<List<EntryView>> open(@PathVariable Long clientId) {
        return ResponseEntity.ok(entryService.findOpenByClient(clientId));
    }

    @PreAuthorize("hasAuthority('read:entries')")
    @GetMapping("/findById/{id}")
    public ResponseEntity<EntryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(entryService.findById(id));
    }

    @PreAuthorize("hasAuthority('read:entries')")
    @GetMapping("/history/{id}")
    public ResponseEntity<List<ChangeHistory>> history(@PathVariable Long id) {
        return ResponseEntity.ok(changeHistoryService.findFor(ChangeHistoryService.ENTRY, id));
    }

    @PreAuthorize("hasAuthority('create:entries')")
    @PostMapping("/insert")
    public ResponseEntity<EntryResponse> insert(@RequestBody @Valid EntryRequest request) {
        return ResponseEntity.ok(entryService.create(request));
    }

    @PreAuthorize("hasAuthority('update:entries')")
    @PutMapping("/update/{id}")
    public ResponseEntity<EntryResponse> update(@PathVariable Long id, @RequestBody @Valid EntryRequest request) {
        return ResponseEntity.ok(entryService.update(id, request));
    }

    @PreAuthorize("hasAuthority('update:entries')")
    @PutMapping("/status/{id}")
    public ResponseEntity<EntryResponse> status(@PathVariable Long id, @RequestBody @Valid StatusRequest request) {
        return ResponseEntity.ok(entryService.changeStatus(id, request));
    }

    @PreAuthorize("hasAuthority('cancel:entries')")
    @PutMapping("/cancel/{id}")
    public ResponseEntity<EntryResponse> cancel(@PathVariable Long id, @RequestBody @Valid CancelRequest request) {
        return ResponseEntity.ok(entryService.cancel(id, request));
    }

    @PreAuthorize("hasAuthority('manage:credit')")
    @PostMapping("/openingBalance")
    public ResponseEntity<EntryResponse> openingBalance(@RequestBody @Valid OpeningBalanceRequest request) {
        return ResponseEntity.ok(entryService.createOpeningBalance(request));
    }
}
