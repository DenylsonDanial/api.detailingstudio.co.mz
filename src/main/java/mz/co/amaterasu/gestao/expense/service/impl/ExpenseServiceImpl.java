package mz.co.amaterasu.gestao.expense.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.service.ChangeHistoryService;
import mz.co.amaterasu.gestao.base.service.DocumentSequenceService;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.CategoryRequest;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseFilter;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseRequest;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseResponse;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ReversalRequest;
import mz.co.amaterasu.gestao.expense.entity.Expense;
import mz.co.amaterasu.gestao.expense.entity.ExpenseCategory;
import mz.co.amaterasu.gestao.expense.repository.ExpenseCategoryRepository;
import mz.co.amaterasu.gestao.expense.repository.ExpenseRepository;
import mz.co.amaterasu.gestao.expense.service.ExpenseService;
import mz.co.amaterasu.gestao.payment.enums.MovementKind;
import mz.co.amaterasu.gestao.payment.enums.MovementStatus;
import mz.co.amaterasu.gestao.util.MaputoTime;
import mz.co.amaterasu.gestao.util.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final DocumentSequenceService documentSequenceService;
    private final ChangeHistoryService changeHistoryService;

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> findAll(ExpenseFilter filter, Pageable pageable) {
        return expenseRepository.findAll(ExpenseRepository.filter(filter), pageable).map(ExpenseResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id) {
        return ExpenseResponse.from(expenseRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Despesa não encontrada.")));
    }

    @Override
    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        ExpenseCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiRequestException("Categoria não encontrada."));
        if (!category.isActive()) {
            throw new ApiRequestException("A categoria está desactivada.");
        }
        LocalDate date = request.getExpenseDate() == null ? MaputoTime.today() : request.getExpenseDate();
        MaputoTime.validateMovementDate(date);

        Expense expense = new Expense();
        expense.setNumber(documentSequenceService.nextNumber(DocumentSequenceService.TYPE_EXPENSE));
        expense.setKind(MovementKind.EXPENSE);
        expense.setDescription(request.getDescription().trim());
        expense.setCategory(category);
        expense.setExpenseDate(date);
        expense.setAmount(Money.of(request.getAmount()));
        expense.setMethod(request.getMethod());
        expense.setNotes(request.getNotes());
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    /** A correccao anula a despesa na sua propria data, deixando rasto e motivo. */
    @Override
    @Transactional
    public ExpenseResponse reverse(Long id, ReversalRequest request) {
        Expense original = expenseRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiRequestException("Despesa não encontrada."));
        if (original.getKind() != MovementKind.EXPENSE) {
            throw new ApiRequestException("Só despesas podem ser corrigidas.");
        }
        if (original.getStatus() == MovementStatus.REVERSED) {
            throw new ApiRequestException("Esta despesa já foi corrigida.");
        }

        Expense reversal = new Expense();
        reversal.setNumber(documentSequenceService.nextNumber(DocumentSequenceService.TYPE_REVERSAL));
        reversal.setKind(MovementKind.REVERSAL);
        reversal.setDescription("Estorno de " + original.getNumber() + " - " + original.getDescription());
        reversal.setCategory(original.getCategory());
        reversal.setExpenseDate(original.getExpenseDate());
        reversal.setAmount(original.getAmount().negate());
        reversal.setMethod(original.getMethod());
        reversal.setReversalOf(original);
        reversal.setReason(request.getReason().trim());

        original.setStatus(MovementStatus.REVERSED);
        expenseRepository.save(original);
        Expense saved = expenseRepository.save(reversal);
        changeHistoryService.record(ChangeHistoryService.EXPENSE, id, "REVERSAL", request.getReason().trim(),
                original.getAmount().toPlainString(), saved.getNumber());
        return ExpenseResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseCategory> findCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public ExpenseCategory saveCategory(Long id, CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id == null ? 0L : id)) {
            throw new ApiRequestException("Já existe uma categoria com este nome.");
        }
        ExpenseCategory category = id == null
                ? new ExpenseCategory()
                : categoryRepository.findById(id).orElseThrow(() -> new ApiRequestException("Categoria não encontrada."));
        category.setName(name);
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        return categoryRepository.save(category);
    }
}
