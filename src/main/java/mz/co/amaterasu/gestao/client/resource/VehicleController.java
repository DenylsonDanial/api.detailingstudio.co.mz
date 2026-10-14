package mz.co.amaterasu.gestao.client.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.ReassignRequest;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.VehicleRequest;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.VehicleResponse;
import mz.co.amaterasu.gestao.client.service.VehicleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @PreAuthorize("hasAuthority('read:vehicles')")
    @GetMapping("/list")
    public Page<VehicleResponse> list(Pageable pageable,
                                      @RequestParam(required = false) RecordStatus status,
                                      @RequestParam(required = false) Long clientId,
                                      @RequestParam(required = false) String term) {
        return vehicleService.findAll(pageable, status, clientId, term);
    }

    @PreAuthorize("hasAuthority('read:vehicles')")
    @GetMapping("/findById/{id}")
    public ResponseEntity<VehicleResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.findById(id));
    }

    /** 204 quando a matricula esta livre. */
    @PreAuthorize("hasAuthority('read:vehicles')")
    @GetMapping("/search")
    public ResponseEntity<VehicleResponse> findByPlate(@RequestParam String plate) {
        return vehicleService.findActiveByPlate(plate)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasAuthority('create:vehicles')")
    @PostMapping("/insert")
    public ResponseEntity<VehicleResponse> insert(@RequestBody @Valid VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.create(request));
    }

    @PreAuthorize("hasAuthority('update:vehicles')")
    @PutMapping("/update/{id}")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id, @RequestBody @Valid VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.update(id, request));
    }

    @PreAuthorize("hasAuthority('reassign:vehicles')")
    @PutMapping("/reassign/{id}")
    public ResponseEntity<VehicleResponse> reassign(@PathVariable Long id, @RequestBody @Valid ReassignRequest request) {
        return ResponseEntity.ok(vehicleService.reassign(id, request));
    }

    @PreAuthorize("hasAuthority('delete:vehicles')")
    @PutMapping("/archive/{id}")
    public ResponseEntity<VehicleResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.setStatus(id, RecordStatus.ARCHIVED));
    }

    @PreAuthorize("hasAuthority('delete:vehicles')")
    @PutMapping("/unarchive/{id}")
    public ResponseEntity<VehicleResponse> unarchive(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.setStatus(id, RecordStatus.ACTIVE));
    }
}
