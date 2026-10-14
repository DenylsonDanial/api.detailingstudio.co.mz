package mz.co.amaterasu.gestao.entry.repository;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import mz.co.amaterasu.gestao.client.entity.Vehicle;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryFilter;
import mz.co.amaterasu.gestao.entry.entity.EntryLine;
import mz.co.amaterasu.gestao.entry.entity.EntryView;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class EntrySpecifications {

    private EntrySpecifications() {
    }

    /** Filtra pela data do servico (local de Maputo), nao pela data de criacao. */
    public static Specification<EntryView> filter(EntryFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("serviceDate"), filter.startDate()));
            }
            if (filter.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("serviceDate"), filter.endDate()));
            }
            if (filter.clientId() != null) {
                predicates.add(cb.equal(root.get("clientId"), filter.clientId()));
            }
            if (filter.vehicleId() != null) {
                predicates.add(cb.equal(root.get("vehicleId"), filter.vehicleId()));
            }
            if (filter.workStatus() != null) {
                predicates.add(cb.equal(root.get("workStatus"), filter.workStatus()));
            }
            if (filter.paymentStatus() != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), filter.paymentStatus()));
            }
            if (filter.serviceId() != null) {
                Subquery<Long> lines = query.subquery(Long.class);
                Root<EntryLine> line = lines.from(EntryLine.class);
                lines.select(line.get("id")).where(
                        cb.equal(line.get("entry").get("id"), root.get("id")),
                        cb.equal(line.get("service").get("id"), filter.serviceId()));
                predicates.add(cb.exists(lines));
            }
            if (filter.term() != null && !filter.term().isBlank()) {
                String like = "%" + filter.term().trim().toLowerCase() + "%";
                String plate = "%" + Vehicle.normalizePlate(filter.term()) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("number")), like),
                        cb.like(cb.lower(root.<String>get("clientName")), like),
                        cb.like(cb.function("replace", String.class,
                                cb.upper(cb.coalesce(root.<String>get("vehiclePlate"), "")),
                                cb.literal(" "), cb.literal("")), plate)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
