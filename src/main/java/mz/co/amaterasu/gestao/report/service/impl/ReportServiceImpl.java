package mz.co.amaterasu.gestao.report.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.client.entity.Client;
import mz.co.amaterasu.gestao.client.repository.ClientRepository;
import mz.co.amaterasu.gestao.document.ReportData;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryFilter;
import mz.co.amaterasu.gestao.entry.entity.EntryView;
import mz.co.amaterasu.gestao.entry.enums.WorkStatus;
import mz.co.amaterasu.gestao.entry.repository.EntrySpecifications;
import mz.co.amaterasu.gestao.entry.repository.EntryViewRepository;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseFilter;
import mz.co.amaterasu.gestao.expense.entity.Expense;
import mz.co.amaterasu.gestao.expense.repository.ExpenseRepository;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentFilter;
import mz.co.amaterasu.gestao.payment.entity.Payment;
import mz.co.amaterasu.gestao.payment.enums.MovementKind;
import mz.co.amaterasu.gestao.payment.enums.PaymentMethod;
import mz.co.amaterasu.gestao.payment.repository.PaymentRepository;
import mz.co.amaterasu.gestao.payment.repository.PaymentSpecifications;
import mz.co.amaterasu.gestao.report.dto.ReportDTOs.MethodAmount;
import mz.co.amaterasu.gestao.report.dto.ReportDTOs.StatementLine;
import mz.co.amaterasu.gestao.report.dto.ReportDTOs.StatementResponse;
import mz.co.amaterasu.gestao.report.dto.ReportDTOs.SummaryResponse;
import mz.co.amaterasu.gestao.report.repository.ReportRepository;
import mz.co.amaterasu.gestao.report.service.ReportService;
import mz.co.amaterasu.gestao.util.MaputoTime;
import mz.co.amaterasu.gestao.util.Money;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    /** Limite de linhas numa exportacao. */
    private static final int EXPORT_LIMIT = 50_000;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(MaputoTime.ZONE);

    private final ReportRepository reportRepository;
    private final ClientRepository clientRepository;
    private final EntryViewRepository entryViewRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional(readOnly = true)
    public SummaryResponse summary(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate == null ? MaputoTime.today() : startDate;
        LocalDate end = endDate == null ? start : endDate;
        if (end.isBefore(start)) {
            throw new ApiRequestException("A data final não pode ser anterior à inicial.");
        }

        SummaryResponse summary = new SummaryResponse();
        summary.setStartDate(start);
        summary.setEndDate(end);

        Object[] services = reportRepository.servicesInPeriod(start, end);
        summary.setEntriesCount(((Number) services[0]).longValue());
        summary.setVehiclesCount(((Number) services[1]).longValue());
        summary.setServicesTotal(Money.of((BigDecimal) services[2]));
        summary.setReceivablePeriod(Money.of((BigDecimal) services[3]));

        summary.setReceiptsByMethod(byMethod(reportRepository.receiptsByMethod(start, end)));
        summary.setReceipts(sum(summary.getReceiptsByMethod()));
        summary.setExpensesByMethod(byMethod(reportRepository.expensesByMethod(start, end)));
        summary.setExpenses(sum(summary.getExpensesByMethod()));
        summary.setNetMovement(Money.of(summary.getReceipts().subtract(summary.getExpenses())));
        summary.setTotalDebt(Money.of(reportRepository.totalDebt()));

        for (WorkStatus status : WorkStatus.values()) {
            summary.getWorkStatusCounts().put(status.name(), 0L);
        }
        for (Object[] row : reportRepository.workStatusInPeriod(start, end)) {
            summary.getWorkStatusCounts().put((String) row[0], ((Number) row[1]).longValue());
        }
        return summary;
    }

    /**
     * Saldo inicial + servicos - pagamentos liquidos (incluindo estornos) = saldo
     * final do periodo. Estornos aparecem a debito, porque repoem a divida.
     */
    @Override
    @Transactional(readOnly = true)
    public StatementResponse statement(Long clientId, LocalDate startDate, LocalDate endDate) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ApiRequestException("Cliente não encontrado."));
        LocalDate end = endDate == null ? MaputoTime.today() : endDate;
        LocalDate start = startDate == null ? end.withDayOfMonth(1) : startDate;
        if (end.isBefore(start)) {
            throw new ApiRequestException("A data final não pode ser anterior à inicial.");
        }

        StatementResponse statement = new StatementResponse();
        statement.setClientId(client.getId());
        statement.setClientName(client.getName());
        statement.setClientPhone(client.getPhone());
        statement.setStartDate(start);
        statement.setEndDate(end);
        statement.setOpeningBalance(Money.of(reportRepository.clientBalanceBefore(clientId, start)));

        List<StatementLine> lines = new ArrayList<>();
        for (Object[] row : reportRepository.clientEntries(clientId, start, end)) {
            boolean opening = Boolean.TRUE.equals(row[4]);
            StatementLine line = new StatementLine();
            line.setDate(toLocalDate(row[0]));
            line.setType(opening ? "OPENING" : "SERVICE");
            line.setReference((String) row[1]);
            String plate = row[2] == null ? "" : " — " + row[2];
            line.setDescription(opening ? "Saldo de abertura" : row[5] + plate);
            line.setDebit(Money.of((BigDecimal) row[3]));
            line.setCredit(Money.ZERO);
            lines.add(line);
        }
        for (Object[] row : reportRepository.clientPayments(clientId, start, end)) {
            MovementKind kind = MovementKind.valueOf((String) row[2]);
            BigDecimal amount = Money.of((BigDecimal) row[3]);
            String method = PaymentMethod.valueOf((String) row[4]).getLabel();
            StatementLine line = new StatementLine();
            line.setDate(toLocalDate(row[0]));
            line.setReference((String) row[1]);
            if (kind == MovementKind.PAYMENT) {
                line.setType("PAYMENT");
                line.setDescription("Pagamento (" + method + ")" + (row[5] == null ? "" : " ref. " + row[5]));
                line.setDebit(Money.ZERO);
                line.setCredit(amount);
            } else {
                boolean refund = Boolean.TRUE.equals(row[7]);
                line.setType(refund ? "REFUND" : "REVERSAL");
                line.setDescription((refund ? "Devolução (" + method + ")" : "Estorno") + " de " + row[5]
                        + (row[6] == null ? "" : ": " + row[6]));
                line.setDebit(amount.negate());
                line.setCredit(Money.ZERO);
            }
            lines.add(line);
        }

        // Ordem estavel: por data; no mesmo dia, servicos antes dos movimentos financeiros.
        lines.sort((a, b) -> {
            int byDate = a.getDate().compareTo(b.getDate());
            if (byDate != 0) {
                return byDate;
            }
            return Integer.compare(order(a.getType()), order(b.getType()));
        });

        BigDecimal running = statement.getOpeningBalance();
        BigDecimal debits = BigDecimal.ZERO;
        BigDecimal credits = BigDecimal.ZERO;
        for (StatementLine line : lines) {
            running = running.add(line.getDebit()).subtract(line.getCredit());
            debits = debits.add(line.getDebit());
            credits = credits.add(line.getCredit());
            line.setBalance(Money.of(running));
        }
        statement.setLines(lines);
        statement.setTotalDebits(Money.of(debits));
        statement.setTotalCredits(Money.of(credits));
        statement.setClosingBalance(Money.of(running));
        statement.setCurrentDebt(Money.of(entryViewRepository.debtByClient(clientId)));
        return statement;
    }

    // --- Exportacoes ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ReportData summaryReport(LocalDate startDate, LocalDate endDate) {
        SummaryResponse summary = summary(startDate, endDate);
        ReportData report = new ReportData("Resumo do período", List.of("Indicador", "Valor"));
        report.addMeta("Período", period(summary.getStartDate(), summary.getEndDate()));
        report.addRow(List.of("Atendimentos (entradas não canceladas)", String.valueOf(summary.getEntriesCount())));
        report.addRow(List.of("Viaturas distintas", String.valueOf(summary.getVehiclesCount())));
        report.addRow(List.of("Serviços registados (MZN)", money(summary.getServicesTotal())));
        report.addRow(List.of("Recebimentos líquidos (MZN)", money(summary.getReceipts())));
        summary.getReceiptsByMethod().forEach(item ->
                report.addRow(List.of("   " + item.getLabel(), money(item.getAmount()))));
        report.addRow(List.of("Despesas pagas (MZN)", money(summary.getExpenses())));
        summary.getExpensesByMethod().forEach(item ->
                report.addRow(List.of("   " + item.getLabel(), money(item.getAmount()))));
        report.addRow(List.of("Movimento líquido (não é lucro nem saldo de caixa)", money(summary.getNetMovement())));
        report.addRow(List.of("Por receber do período (MZN)", money(summary.getReceivablePeriod())));
        report.addRow(List.of("Dívida total actual (MZN)", money(summary.getTotalDebt())));
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportData statementReport(Long clientId, LocalDate startDate, LocalDate endDate) {
        StatementResponse statement = statement(clientId, startDate, endDate);
        ReportData report = new ReportData("Extracto de conta corrente",
                List.of("Data", "Referência", "Descrição", "Débito", "Crédito", "Saldo"));
        report.setSubtitle(statement.getClientName());
        report.addMeta("Período", period(statement.getStartDate(), statement.getEndDate()));
        if (statement.getClientPhone() != null) {
            report.addMeta("Contacto", statement.getClientPhone());
        }
        report.addMeta("Saldo inicial", money(statement.getOpeningBalance()) + " MZN");
        report.addMeta("Saldo final do período", money(statement.getClosingBalance()) + " MZN");
        report.addMeta("Dívida actual (hoje)", money(statement.getCurrentDebt()) + " MZN");

        report.addRow(List.of(statement.getStartDate().format(DATE), "", "Saldo inicial", "", "",
                money(statement.getOpeningBalance())));
        for (StatementLine line : statement.getLines()) {
            report.addRow(List.of(line.getDate().format(DATE), line.getReference(), line.getDescription(),
                    blankIfZero(line.getDebit()), blankIfZero(line.getCredit()), money(line.getBalance())));
        }
        report.setTotals(List.of("", "", "Totais", money(statement.getTotalDebits()),
                money(statement.getTotalCredits()), money(statement.getClosingBalance())));
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportData entriesReport(EntryFilter filter) {
        List<EntryView> entries = entryViewRepository.findAll(EntrySpecifications.filter(filter),
                exportPage(Sort.by("entryAt", "id"))).getContent();
        ReportData report = new ReportData("Entradas de viaturas", List.of("Número", "Data", "Cliente", "Matrícula",
                "Trabalho", "Total", "Pago", "Saldo", "Pagamento"));
        report.addMeta("Período", period(filter.startDate(), filter.endDate()));
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal balance = BigDecimal.ZERO;
        for (EntryView entry : entries) {
            report.addRow(List.of(entry.getNumber(), DATE_TIME.format(entry.getEntryAt()), entry.getClientName(),
                    entry.getVehiclePlate() == null ? "" : entry.getVehiclePlate(), entry.getWorkStatus().getLabel(),
                    money(entry.getTotal()), money(entry.getPaid()), money(entry.getBalance()),
                    entry.getPaymentStatus().getLabel()));
            if (entry.getWorkStatus() != WorkStatus.CANCELLED) {
                total = total.add(entry.getTotal());
                paid = paid.add(entry.getPaid());
                balance = balance.add(entry.getBalance());
            }
        }
        report.setTotals(List.of("Total (sem canceladas)", "", "", "", "", money(total), money(paid), money(balance), ""));
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportData paymentsReport(PaymentFilter filter) {
        List<Payment> payments = paymentRepository.findAll(PaymentSpecifications.filter(filter),
                exportPage(Sort.by("paymentDate", "id"))).getContent();
        ReportData report = new ReportData("Recebimentos", List.of("Número", "Data", "Cliente", "Tipo", "Método",
                "Referência", "Montante", "Motivo"));
        report.addMeta("Período", period(filter.startDate(), filter.endDate()));
        BigDecimal total = BigDecimal.ZERO;
        for (Payment payment : payments) {
            report.addRow(List.of(payment.getNumber(), payment.getPaymentDate().format(DATE), payment.getClientName(),
                    payment.getKind() == MovementKind.PAYMENT ? "Pagamento" : (payment.isRefund() ? "Devolução" : "Estorno"),
                    payment.getMethod().getLabel(), nullToEmpty(payment.getReference()), money(payment.getAmount()),
                    nullToEmpty(payment.getReason())));
            total = total.add(payment.getAmount());
        }
        report.setTotals(List.of("Total líquido", "", "", "", "", "", money(total), ""));
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportData expensesReport(ExpenseFilter filter) {
        List<Expense> expenses = expenseRepository.findAll(ExpenseRepository.filter(filter),
                exportPage(Sort.by("expenseDate", "id"))).getContent();
        ReportData report = new ReportData("Despesas pagas", List.of("Número", "Data", "Descrição", "Categoria",
                "Método", "Valor", "Motivo da correcção"));
        report.addMeta("Período", period(filter.startDate(), filter.endDate()));
        BigDecimal total = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            report.addRow(List.of(expense.getNumber(), expense.getExpenseDate().format(DATE), expense.getDescription(),
                    expense.getCategory().getName(), expense.getMethod().getLabel(), money(expense.getAmount()),
                    nullToEmpty(expense.getReason())));
            total = total.add(expense.getAmount());
        }
        report.setTotals(List.of("Total líquido", "", "", "", "", money(total), ""));
        return report;
    }

    // ------------------------------------------------------------------------

    private static List<MethodAmount> byMethod(List<Object[]> rows) {
        List<MethodAmount> result = new ArrayList<>();
        for (Object[] row : rows) {
            PaymentMethod method = PaymentMethod.valueOf((String) row[0]);
            result.add(new MethodAmount(method, method.getLabel(), Money.of((BigDecimal) row[1])));
        }
        return result;
    }

    private static BigDecimal sum(List<MethodAmount> items) {
        return Money.of(items.stream().map(MethodAmount::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static int order(String type) {
        return switch (type) {
            case "OPENING" -> 0;
            case "SERVICE" -> 1;
            default -> 2;
        };
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        return ((Date) value).toLocalDate();
    }

    private static Pageable exportPage(Sort sort) {
        return PageRequest.of(0, EXPORT_LIMIT, sort);
    }

    private static String period(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return "Todos";
        }
        return (start == null ? "…" : start.format(DATE)) + " a " + (end == null ? "…" : end.format(DATE));
    }

    private static String money(BigDecimal value) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-PT")));
        format.setGroupingSize(3);
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        format.setDecimalFormatSymbols(symbols);
        return format.format(Money.of(value));
    }

    private static String blankIfZero(BigDecimal value) {
        return value == null || value.signum() == 0 ? "" : money(value);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
