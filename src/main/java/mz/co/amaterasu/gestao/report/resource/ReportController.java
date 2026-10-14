package mz.co.amaterasu.gestao.report.resource;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.document.ExportStrategy;
import mz.co.amaterasu.gestao.document.ReportData;
import mz.co.amaterasu.gestao.document.StrategyFactory;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryFilter;
import mz.co.amaterasu.gestao.entry.enums.EntryPaymentStatus;
import mz.co.amaterasu.gestao.entry.enums.WorkStatus;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseFilter;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentFilter;
import mz.co.amaterasu.gestao.payment.enums.MovementKind;
import mz.co.amaterasu.gestao.payment.enums.PaymentMethod;
import mz.co.amaterasu.gestao.report.service.ReportService;
import mz.co.amaterasu.gestao.util.MaputoTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

/** Exportacoes para Excel ou PDF. Aplicam exactamente os mesmos filtros das listagens. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/reports/export")
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasAuthority('export:dashboard')")
    @GetMapping("/summary")
    public ResponseEntity<byte[]> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "EXCEL") String fileFormat) {
        return file(reportService.summaryReport(startDate, endDate), "resumo", fileFormat);
    }

    @PreAuthorize("hasAuthority('export:statements')")
    @GetMapping("/statement/{clientId}")
    public ResponseEntity<byte[]> statement(
            @PathVariable Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "PDF") String fileFormat) {
        return file(reportService.statementReport(clientId, startDate, endDate), "extracto-" + clientId, fileFormat);
    }

    @PreAuthorize("hasAuthority('export:entries')")
    @GetMapping("/entries")
    public ResponseEntity<byte[]> entries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) WorkStatus workStatus,
            @RequestParam(required = false) EntryPaymentStatus paymentStatus,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String term,
            @RequestParam(defaultValue = "EXCEL") String fileFormat) {
        return file(reportService.entriesReport(new EntryFilter(startDate, endDate, clientId, vehicleId, workStatus,
                paymentStatus, serviceId, term)), "entradas", fileFormat);
    }

    @PreAuthorize("hasAuthority('export:payments')")
    @GetMapping("/payments")
    public ResponseEntity<byte[]> payments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) MovementKind kind,
            @RequestParam(required = false) String term,
            @RequestParam(defaultValue = "EXCEL") String fileFormat) {
        return file(reportService.paymentsReport(new PaymentFilter(startDate, endDate, clientId, method, kind, term)),
                "recebimentos", fileFormat);
    }

    @PreAuthorize("hasAuthority('export:expenses')")
    @GetMapping("/expenses")
    public ResponseEntity<byte[]> expenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) String term,
            @RequestParam(defaultValue = "EXCEL") String fileFormat) {
        return file(reportService.expensesReport(new ExpenseFilter(startDate, endDate, categoryId, method, term)),
                "despesas", fileFormat);
    }

    private ResponseEntity<byte[]> file(ReportData data, String baseName, String fileFormat) {
        ExportStrategy strategy = StrategyFactory.getStrategy(fileFormat);
        try {
            byte[] content = strategy.export(data);
            String fileName = baseName + "-" + MaputoTime.today() + "." + strategy.fileExtension();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(strategy.contentType()))
                    .body(content);
        } catch (IOException e) {
            throw new ApiRequestException("Não foi possível gerar o ficheiro de exportação.");
        }
    }
}
