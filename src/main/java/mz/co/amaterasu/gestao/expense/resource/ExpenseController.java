package mz.co.amaterasu.gestao.expense.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.CategoryRequest;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseFilter;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseRequest;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ExpenseResponse;
import mz.co.amaterasu.gestao.expense.dto.ExpenseDTOs.ReversalRequest;
import mz.co.amaterasu.gestao.expense.entity.ExpenseCategory;
import mz.co.amaterasu.gestao.expense.service.ExpenseService;
import mz.co.amaterasu.gestao.payment.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PreAuthorize("hasAuthority('read:expenses')")
    @GetMapping("/list")
    public Page<ExpenseResponse> list(Pageable pageable,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                      @RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) PaymentMethod method,
                                      @RequestParam(required = false) String term) {
        return expenseService.findAll(new ExpenseFilter(startDate, endDate, categoryId, method, term), pageable);
    }

    @PreAuthorize("hasAuthority('read:expenses')")
    @GetMapping("/findById/{id}")
    public ResponseEntity<ExpenseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.findById(id));
    }

    @PreAuthorize("hasAuthority('create:expenses')")
    @PostMapping("/insert")
    public ResponseEntity<ExpenseResponse> insert(@RequestBody @Valid ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.create(request));
    }

    @PreAuthorize("hasAuthority('reverse:expenses')")
    @PostMapping("/reverse/{id}")
    public ResponseEntity<ExpenseResponse> reverse(@PathVariable Long id, @RequestBody @Valid ReversalRequest request) {
        return ResponseEntity.ok(expenseService.reverse(id, request));
    }

    @PreAuthorize("hasAuthority('read:expenses')")
    @GetMapping("/categories/list")
    public Page<ExpenseCategory> categories(Pageable pageable) {
        return expenseService.findCategories(pageable);
    }

    @PreAuthorize("hasAuthority('update:expenses')")
    @PostMapping("/categories/insert")
    public ResponseEntity<ExpenseCategory> insertCategory(@RequestBody @Valid CategoryRequest request) {
        return ResponseEntity.ok(expenseService.saveCategory(null, request));
    }

    @PreAuthorize("hasAuthority('update:expenses')")
    @PutMapping("/categories/update/{id}")
    public ResponseEntity<ExpenseCategory> updateCategory(@PathVariable Long id,
                                                          @RequestBody @Valid CategoryRequest request) {
        return ResponseEntity.ok(expenseService.saveCategory(id, request));
    }
}
