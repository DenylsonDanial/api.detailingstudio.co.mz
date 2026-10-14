package mz.co.amaterasu.gestao.payment.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.AllocationPreview;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentFilter;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentRequest;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentResponse;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.ReversalRequest;
import mz.co.amaterasu.gestao.payment.enums.MovementKind;
import mz.co.amaterasu.gestao.payment.enums.PaymentMethod;
import mz.co.amaterasu.gestao.payment.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasAuthority('read:payments')")
    @GetMapping("/list")
    public Page<PaymentResponse> list(Pageable pageable,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                      @RequestParam(required = false) Long clientId,
                                      @RequestParam(required = false) PaymentMethod method,
                                      @RequestParam(required = false) MovementKind kind,
                                      @RequestParam(required = false) String term) {
        return paymentService.findAll(new PaymentFilter(startDate, endDate, clientId, method, kind, term), pageable);
    }

    @PreAuthorize("hasAuthority('read:payments')")
    @GetMapping("/findById/{id}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @PreAuthorize("hasAuthority('create:payments')")
    @GetMapping("/previewAllocation")
    public ResponseEntity<List<AllocationPreview>> previewAllocation(@RequestParam Long clientId,
                                                                     @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(paymentService.previewAllocation(clientId, amount));
    }

    @PreAuthorize("hasAuthority('create:payments')")
    @PostMapping("/insert")
    public ResponseEntity<PaymentResponse> insert(@RequestBody @Valid PaymentRequest request) {
        return ResponseEntity.ok(paymentService.create(request));
    }

    @PreAuthorize("hasAuthority('reverse:payments')")
    @PostMapping("/reverse/{id}")
    public ResponseEntity<PaymentResponse> reverse(@PathVariable Long id, @RequestBody @Valid ReversalRequest request) {
        return ResponseEntity.ok(paymentService.reverse(id, request));
    }
}
