package mz.co.amaterasu.gestao.report.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Agregados dos indicadores e do extracto. Cada indicador usa a sua propria data:
 * servicos pela data do servico, recebimentos pela data de recebimento e
 * despesas pela data de pagamento (especificacao, seccao 8).
 */
@Repository
public class ReportRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /** [entradas, viaturas distintas, total de servicos, saldo actual] das entradas validas do periodo. */
    public Object[] servicesInPeriod(LocalDate start, LocalDate end) {
        return (Object[]) entityManager.createNativeQuery(
                        "select count(*), count(distinct vehicle_id), coalesce(sum(total), 0), coalesce(sum(balance), 0) " +
                                "from vw_entries where work_status <> 'CANCELLED' and not opening_balance " +
                                "and service_date between :start and :end")
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> workStatusInPeriod(LocalDate start, LocalDate end) {
        return entityManager.createNativeQuery(
                        "select work_status, count(*) from vw_entries where not opening_balance " +
                                "and service_date between :start and :end group by work_status")
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    /** Recebimentos liquidos (estornos com sinal negativo) por metodo. */
    @SuppressWarnings("unchecked")
    public List<Object[]> receiptsByMethod(LocalDate start, LocalDate end) {
        return entityManager.createNativeQuery(
                        "select method, coalesce(sum(amount), 0) from tb_payments " +
                                "where payment_date between :start and :end group by method order by method")
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> expensesByMethod(LocalDate start, LocalDate end) {
        return entityManager.createNativeQuery(
                        "select method, coalesce(sum(amount), 0) from tb_expenses " +
                                "where expense_date between :start and :end group by method order by method")
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    public BigDecimal totalDebt() {
        return (BigDecimal) entityManager.createNativeQuery("select coalesce(sum(balance), 0) from vw_entries")
                .getSingleResult();
    }

    /** Saldo historico do cliente no inicio do dia indicado. */
    public BigDecimal clientBalanceBefore(Long clientId, LocalDate date) {
        BigDecimal services = (BigDecimal) entityManager.createNativeQuery(
                        "select coalesce(sum(total), 0) from vw_entries where client_id = :client " +
                                "and work_status <> 'CANCELLED' and service_date < :date")
                .setParameter("client", clientId)
                .setParameter("date", date)
                .getSingleResult();
        BigDecimal payments = (BigDecimal) entityManager.createNativeQuery(
                        "select coalesce(sum(amount), 0) from tb_payments where client_id = :client and payment_date < :date")
                .setParameter("client", clientId)
                .setParameter("date", date)
                .getSingleResult();
        return services.subtract(payments);
    }

    /** [data, numero, matricula, total, saldo de abertura?, servicos] das entradas validas do periodo. */
    @SuppressWarnings("unchecked")
    public List<Object[]> clientEntries(Long clientId, LocalDate start, LocalDate end) {
        return entityManager.createNativeQuery(
                        "select e.service_date, e.number, e.vehicle_plate, e.total, e.opening_balance, " +
                                "(select string_agg(l.service_name, ', ' order by l.sort_order) " +
                                " from tb_entry_lines l where l.entry_id = e.id) " +
                                "from vw_entries e where e.client_id = :client and e.work_status <> 'CANCELLED' " +
                                "and e.service_date between :start and :end order by e.entry_at, e.id")
                .setParameter("client", clientId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    /** [data, numero, tipo, montante, metodo, referencia, motivo, devolucao?] dos movimentos do periodo. */
    @SuppressWarnings("unchecked")
    public List<Object[]> clientPayments(Long clientId, LocalDate start, LocalDate end) {
        return entityManager.createNativeQuery(
                        "select payment_date, number, kind, amount, method, reference, reason, refund " +
                                "from tb_payments where client_id = :client and payment_date between :start and :end " +
                                "order by payment_date, created_at, id")
                .setParameter("client", clientId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }
}
