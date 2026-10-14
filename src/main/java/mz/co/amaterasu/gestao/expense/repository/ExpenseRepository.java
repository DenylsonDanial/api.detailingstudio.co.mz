package mz.co.amaterasu.gestao.expense.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.Predicate;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseFilter;
import mz.co.amaterasu.gestao.expense.entity.Expense;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Expense e where e.id = :id")
    Optional<Expense> findByIdForUpdate(@Param("id") Long id);

    /** Filtra pela data de pagamento da despesa. */
    static Specification<Expense> filter(ExpenseFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), filter.startDate()));
            }
            if (filter.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), filter.endDate()));
            }
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }
            if (filter.method() != null) {
                predicates.add(cb.equal(root.get("method"), filter.method()));
            }
            if (filter.term() != null && !filter.term().isBlank()) {
                String like = "%" + filter.term().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("number")), like),
                        cb.like(cb.lower(root.<String>get("description")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
