package mz.co.amaterasu.gestao.payment.repository;

import jakarta.persistence.criteria.Predicate;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentFilter;
import mz.co.amaterasu.gestao.payment.entity.Payment;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class PaymentSpecifications {

    private PaymentSpecifications() {
    }

    /** Filtra pela data de recebimento, nao pela data do servico. */
    public static Specification<Payment> filter(PaymentFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), filter.startDate()));
            }
            if (filter.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), filter.endDate()));
            }
            if (filter.clientId() != null) {
                predicates.add(cb.equal(root.get("client").get("id"), filter.clientId()));
            }
            if (filter.method() != null) {
                predicates.add(cb.equal(root.get("method"), filter.method()));
            }
            if (filter.kind() != null) {
                predicates.add(cb.equal(root.get("kind"), filter.kind()));
            }
            if (filter.term() != null && !filter.term().isBlank()) {
                String like = "%" + filter.term().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("number")), like),
                        cb.like(cb.lower(root.<String>get("clientName")), like),
                        cb.like(cb.lower(cb.coalesce(root.<String>get("reference"), "")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
