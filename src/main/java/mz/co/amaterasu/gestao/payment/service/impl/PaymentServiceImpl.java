package mz.co.amaterasu.gestao.payment.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.service.ChangeHistoryService;
import mz.co.amaterasu.gestao.base.service.DocumentSequenceService;
import mz.co.amaterasu.gestao.client.entity.Client;
import mz.co.amaterasu.gestao.client.repository.ClientRepository;
import mz.co.amaterasu.gestao.entry.entity.Entry;
import mz.co.amaterasu.gestao.entry.entity.EntryView;
import mz.co.amaterasu.gestao.entry.enums.WorkStatus;
import mz.co.amaterasu.gestao.entry.repository.EntryRepository;
import mz.co.amaterasu.gestao.entry.repository.EntryViewRepository;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.AllocationPreview;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.AllocationRequest;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentFilter;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentRequest;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentResponse;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.ReversalRequest;
import mz.co.amaterasu.gestao.payment.entity.Payment;
import mz.co.amaterasu.gestao.payment.entity.PaymentAllocation;
import mz.co.amaterasu.gestao.payment.enums.MovementKind;
import mz.co.amaterasu.gestao.payment.enums.MovementStatus;
import mz.co.amaterasu.gestao.payment.repository.PaymentAllocationRepository;
import mz.co.amaterasu.gestao.payment.repository.PaymentRepository;
import mz.co.amaterasu.gestao.payment.repository.PaymentSpecifications;
import mz.co.amaterasu.gestao.payment.service.PaymentService;
import mz.co.amaterasu.gestao.util.MaputoTime;
import mz.co.amaterasu.gestao.util.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final ClientRepository clientRepository;
    private final EntryRepository entryRepository;
    private final EntryViewRepository entryViewRepository;
    private final DocumentSequenceService documentSequenceService;
    private final ChangeHistoryService changeHistoryService;

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> findAll(PaymentFilter filter, Pageable pageable) {
        return paymentRepository.findAll(PaymentSpecifications.filter(filter), pageable).map(PaymentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse findById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Pagamento não encontrado."));
        return PaymentResponse.from(payment, allocationRepository.findByPayment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationPreview> previewAllocation(Long clientId, BigDecimal amount) {
        BigDecimal remaining = Money.of(amount);
        List<AllocationPreview> preview = new ArrayList<>();
        for (EntryView entry : entryViewRepository.findOpenByClient(clientId)) {
            AllocationPreview item = new AllocationPreview();
            item.setEntryId(entry.getId());
            item.setEntryNumber(entry.getNumber());
            item.setEntryAt(entry.getEntryAt());
            item.setVehiclePlate(entry.getVehiclePlate());
            item.setTotal(entry.getTotal());
            item.setBalance(Money.of(entry.getBalance()));
            BigDecimal applied = remaining.min(entry.getBalance()).max(BigDecimal.ZERO);
            item.setAmount(Money.of(applied));
            remaining = remaining.subtract(applied);
            preview.add(item);
        }
        return preview;
    }

    /**
     * Pagamento e afectacoes gravam-se na mesma transaccao. O cliente e as
     * entradas afectadas ficam bloqueados (SELECT ... FOR UPDATE) e os saldos sao
     * recalculados depois do bloqueio, o que impede sobrepagamento por pedidos
     * simultaneos; a chave de idempotencia trata o duplo clique.
     */
    @Override
    @Transactional
    public PaymentResponse create(PaymentRequest request) {
        BigDecimal amount = Money.of(request.getAmount());
        if (amount.signum() <= 0) {
            throw new ApiRequestException("O montante deve ser positivo.");
        }
        LocalDate date = request.getPaymentDate() == null ? MaputoTime.today() : request.getPaymentDate();
        MaputoTime.validateMovementDate(date);

        Client client = clientRepository.findByIdForUpdate(request.getClientId())
                .orElseThrow(() -> new ApiRequestException("Cliente não encontrado."));

        String key = request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()
                ? null : request.getIdempotencyKey().trim();
        if (key != null) {
            var existing = paymentRepository.findByIdempotencyKey(key);
            if (existing.isPresent()) {
                return findById(existing.get().getId());
            }
        }

        List<Long> openIds = entryViewRepository.findOpenByClient(client.getId()).stream()
                .map(EntryView::getId).toList();
        Set<Long> requestedIds = new HashSet<>();
        request.getAllocations().forEach(allocation -> requestedIds.add(allocation.getEntryId()));
        Set<Long> toLock = new HashSet<>(openIds);
        toLock.addAll(requestedIds);
        if (toLock.isEmpty()) {
            throw new ApiRequestException("O cliente não tem valores por pagar.");
        }

        List<Entry> locked = entryRepository.findAllByIdForUpdate(toLock);
        Map<Long, BigDecimal> balances = balancesAfterLock(locked, client);
        BigDecimal debt = balances.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amount.compareTo(debt) > 0) {
            throw new ApiRequestException(String.format(
                    "O montante (%s MZN) excede a dívida actual do cliente (%s MZN). Adiantamentos não são permitidos.",
                    amount.toPlainString(), Money.of(debt).toPlainString()));
        }

        Map<Long, BigDecimal> distribution = request.getAllocations().isEmpty()
                ? distributeOldestFirst(balances, amount)
                : validateManual(request.getAllocations(), balances, amount);

        Payment payment = new Payment();
        payment.setNumber(documentSequenceService.nextNumber(DocumentSequenceService.TYPE_RECEIPT));
        payment.setClient(client);
        payment.setClientName(client.getName());
        payment.setKind(MovementKind.PAYMENT);
        payment.setPaymentDate(date);
        payment.setAmount(amount);
        payment.setMethod(request.getMethod());
        payment.setReference(blankToNull(request.getReference()));
        payment.setNotes(blankToNull(request.getNotes()));
        payment.setIdempotencyKey(key);

        Map<Long, Entry> entriesById = new HashMap<>();
        locked.forEach(entry -> entriesById.put(entry.getId(), entry));
        distribution.forEach((entryId, value) -> {
            PaymentAllocation allocation = new PaymentAllocation();
            allocation.setPayment(payment);
            allocation.setEntry(entriesById.get(entryId));
            allocation.setAmount(value);
            payment.getAllocations().add(allocation);
        });

        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.from(saved, saved.getAllocations());
    }

    /**
     * Movimentos financeiros nao se apagam: o estorno e um novo lancamento de
     * sinal contrario, com motivo e ligacao ao original, que repoe os saldos.
     * Uma correccao de registo fica na data do original (anula-o nos relatorios);
     * uma devolucao efectiva fica na data e no metodo em que o dinheiro saiu.
     */
    @Override
    @Transactional
    public PaymentResponse reverse(Long id, ReversalRequest request) {
        Payment original = paymentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiRequestException("Pagamento não encontrado."));
        if (original.getKind() != MovementKind.PAYMENT) {
            throw new ApiRequestException("Só pagamentos podem ser estornados.");
        }
        if (original.getStatus() == MovementStatus.REVERSED) {
            throw new ApiRequestException("Este pagamento já foi estornado.");
        }
        clientRepository.findByIdForUpdate(original.getClient().getId());
        List<PaymentAllocation> allocations = allocationRepository.findByPayment(id);
        entryRepository.findAllByIdForUpdate(allocations.stream().map(a -> a.getEntry().getId()).toList());

        LocalDate date = original.getPaymentDate();
        var method = original.getMethod();
        if (request.isRefund()) {
            date = request.getRefundDate() == null ? MaputoTime.today() : request.getRefundDate();
            MaputoTime.validateMovementDate(date);
            if (request.getRefundMethod() != null) {
                method = request.getRefundMethod();
            }
        }

        Payment reversal = new Payment();
        reversal.setNumber(documentSequenceService.nextNumber(DocumentSequenceService.TYPE_REVERSAL));
        reversal.setClient(original.getClient());
        reversal.setClientName(original.getClientName());
        reversal.setKind(MovementKind.REVERSAL);
        reversal.setPaymentDate(date);
        reversal.setAmount(original.getAmount().negate());
        reversal.setMethod(method);
        reversal.setReference(original.getNumber());
        reversal.setReversalOf(original);
        reversal.setReason(request.getReason().trim());
        reversal.setRefund(request.isRefund());
        for (PaymentAllocation source : allocations) {
            PaymentAllocation allocation = new PaymentAllocation();
            allocation.setPayment(reversal);
            allocation.setEntry(source.getEntry());
            allocation.setAmount(source.getAmount().negate());
            reversal.getAllocations().add(allocation);
        }

        original.setStatus(MovementStatus.REVERSED);
        paymentRepository.save(original);
        Payment saved = paymentRepository.save(reversal);
        changeHistoryService.record(ChangeHistoryService.PAYMENT, original.getId(),
                request.isRefund() ? "REFUND" : "REVERSAL", request.getReason().trim(),
                original.getAmount().toPlainString(), saved.getNumber());
        return PaymentResponse.from(saved, saved.getAllocations());
    }

    // ------------------------------------------------------------------------

    private Map<Long, BigDecimal> balancesAfterLock(List<Entry> locked, Client client) {
        Map<Long, BigDecimal> paid = new HashMap<>();
        for (Object[] row : allocationRepository.paidByEntries(locked.stream().map(Entry::getId).toList())) {
            paid.put((Long) row[0], (BigDecimal) row[1]);
        }
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        for (Entry entry : locked) {
            if (!entry.getClient().getId().equals(client.getId())) {
                throw new ApiRequestException(String.format(
                        "A entrada %s não pertence a este cliente.", entry.getNumber()));
            }
            if (entry.getWorkStatus() == WorkStatus.CANCELLED) {
                continue;
            }
            BigDecimal balance = Money.of(entry.getTotal().subtract(paid.getOrDefault(entry.getId(), BigDecimal.ZERO)));
            if (balance.signum() > 0) {
                balances.put(entry.getId(), balance);
            }
        }
        return balances;
    }

    /** Liquida primeiro as entradas mais antigas (a ordem do bloqueio). */
    private Map<Long, BigDecimal> distributeOldestFirst(Map<Long, BigDecimal> balances, BigDecimal amount) {
        Map<Long, BigDecimal> distribution = new LinkedHashMap<>();
        BigDecimal remaining = amount;
        for (Map.Entry<Long, BigDecimal> balance : balances.entrySet()) {
            if (remaining.signum() <= 0) {
                break;
            }
            BigDecimal applied = remaining.min(balance.getValue());
            distribution.put(balance.getKey(), Money.of(applied));
            remaining = remaining.subtract(applied);
        }
        return distribution;
    }

    private Map<Long, BigDecimal> validateManual(List<AllocationRequest> requested, Map<Long, BigDecimal> balances,
                                                 BigDecimal amount) {
        Map<Long, BigDecimal> distribution = new LinkedHashMap<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (AllocationRequest allocation : requested) {
            BigDecimal value = Money.of(allocation.getAmount());
            if (value.signum() <= 0) {
                continue;
            }
            if (distribution.containsKey(allocation.getEntryId())) {
                throw new ApiRequestException("A mesma entrada aparece mais do que uma vez na distribuição.");
            }
            BigDecimal balance = balances.get(allocation.getEntryId());
            if (balance == null) {
                throw new ApiRequestException("Uma das entradas seleccionadas já não tem saldo por pagar.");
            }
            if (value.compareTo(balance) > 0) {
                throw new ApiRequestException(String.format(
                        "O valor afectado (%s MZN) excede o saldo da entrada (%s MZN).",
                        value.toPlainString(), balance.toPlainString()));
            }
            distribution.put(allocation.getEntryId(), value);
            sum = sum.add(value);
        }
        if (sum.compareTo(amount) != 0) {
            throw new ApiRequestException(String.format(
                    "A soma afectada (%s MZN) tem de ser igual ao pagamento (%s MZN).",
                    Money.of(sum).toPlainString(), amount.toPlainString()));
        }
        return distribution;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
