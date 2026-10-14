package mz.co.amaterasu.gestao.report.resource;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.report.dto.ReportDTOs.StatementResponse;
import mz.co.amaterasu.gestao.report.dto.ReportDTOs.SummaryResponse;
import mz.co.amaterasu.gestao.report.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class DashboardController {

    private final ReportService reportService;

    /** Sem datas: hoje, no fuso Africa/Maputo. */
    @PreAuthorize("hasAuthority('read:dashboard')")
    @GetMapping("/dashboard/summary")
    public ResponseEntity<SummaryResponse> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.summary(startDate, endDate));
    }

    @PreAuthorize("hasAuthority('read:statements')")
    @GetMapping("/statements/{clientId}")
    public ResponseEntity<StatementResponse> statement(
            @PathVariable Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.statement(clientId, startDate, endDate));
    }
}
