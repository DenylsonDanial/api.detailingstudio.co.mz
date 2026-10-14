package mz.co.amaterasu.gestao.base.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.dto.BusinessSettingDTO;
import mz.co.amaterasu.gestao.base.service.BusinessSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/business")
public class BusinessSettingController {

    private final BusinessSettingService businessSettingService;

    @PreAuthorize("hasAuthority('read:settings')")
    @GetMapping("/findFirst")
    public ResponseEntity<BusinessSettingDTO> findFirst() {
        return ResponseEntity.ok(businessSettingService.findFirst());
    }

    @PreAuthorize("hasAuthority('update:settings')")
    @PutMapping("/update")
    public ResponseEntity<BusinessSettingDTO> update(@RequestBody @Valid BusinessSettingDTO dto) {
        return ResponseEntity.ok(businessSettingService.update(dto));
    }
}
