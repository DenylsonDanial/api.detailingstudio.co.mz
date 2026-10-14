package mz.co.amaterasu.gestao.report.service;

import mz.co.amaterasu.gestao.document.ReportData;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryFilter;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseFilter;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentFilter;
import mz.co.amaterasu.gestao.report.dto.ReportDTOs.StatementResponse;
import mz.co.amaterasu.gestao.report.dto.ReportDTOs.SummaryResponse;

import java.time.LocalDate;

public interface ReportService {

    SummaryResponse summary(LocalDate startDate, LocalDate endDate);

    StatementResponse statement(Long clientId, LocalDate startDate, LocalDate endDate);

    ReportData summaryReport(LocalDate startDate, LocalDate endDate);

    ReportData statementReport(Long clientId, LocalDate startDate, LocalDate endDate);

    ReportData entriesReport(EntryFilter filter);

    ReportData paymentsReport(PaymentFilter filter);

    ReportData expensesReport(ExpenseFilter filter);
}
