package mz.co.amaterasu.gestao.expense.service;

import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.CategoryRequest;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseFilter;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseRequest;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseResponse;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ReversalRequest;
import mz.co.amaterasu.gestao.expense.entity.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpenseService {

    Page<ExpenseResponse> findAll(ExpenseFilter filter, Pageable pageable);

    ExpenseResponse findById(Long id);

    ExpenseResponse create(ExpenseRequest request);

    ExpenseResponse reverse(Long id, ReversalRequest request);

    Page<ExpenseCategory> findCategories(Pageable pageable);

    ExpenseCategory saveCategory(Long id, CategoryRequest request);
}
