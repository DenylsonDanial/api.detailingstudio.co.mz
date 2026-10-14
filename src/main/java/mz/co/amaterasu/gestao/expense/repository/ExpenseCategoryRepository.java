package mz.co.amaterasu.gestao.expense.repository;

import mz.co.amaterasu.gestao.expense.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
