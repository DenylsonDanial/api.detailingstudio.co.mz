package mz.co.amaterasu.gestao.entry.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.base.service.ChangeHistoryService;
import mz.co.amaterasu.gestao.base.service.DocumentSequenceService;
import mz.co.amaterasu.gestao.catalog.entity.ServiceItem;
import mz.co.amaterasu.gestao.catalog.enums.PricingType;
import mz.co.amaterasu.gestao.catalog.repository.ServiceItemRepository;
import mz.co.amaterasu.gestao.client.entity.Client;
import mz.co.amaterasu.gestao.client.entity.Vehicle;
import mz.co.amaterasu.gestao.client.enums.VehicleCategory;
import mz.co.amaterasu.gestao.client.repository.ClientRepository;
import mz.co.amaterasu.gestao.client.repository.VehicleRepository;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.CancelRequest;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryFilter;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryRequest;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryResponse;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.LineRequest;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.OpeningBalanceRequest;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.StatusRequest;
import mz.co.amaterasu.gestao.entry.entity.Entry;
import mz.co.amaterasu.gestao.entry.entity.EntryLine;
import mz.co.amaterasu.gestao.entry.entity.EntryView;
import mz.co.amaterasu.gestao.entry.enums.WorkStatus;
import mz.co.amaterasu.gestao.entry.repository.EntryRepository;
import mz.co.amaterasu.gestao.entry.repository.EntrySpecifications;
import mz.co.amaterasu.gestao.entry.repository.EntryViewRepository;
import mz.co.amaterasu.gestao.entry.service.EntryService;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.payment.repository.PaymentAllocationRepository;
import mz.co.amaterasu.gestao.util.MaputoTime;
import mz.co.amaterasu.gestao.util.Money;
import mz.co.amaterasu.gestao.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryServiceImpl implements EntryService {

    private static final String MANAGE_DELIVERED = "manage:delivered";

    private final EntryRepository entryRepository;
    private final EntryViewRepository entryViewRepository;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final DocumentSequenceService documentSequenceService;
    private final ChangeHistoryService changeHistoryService;

    @Override
    @Transactional(readOnly = true)
    public Page<EntryView> findAll(EntryFilter filter, Pageable pageable) {
        return entryViewRepository.findAll(EntrySpecifications.filter(filter), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntryView> findOpenByClient(Long clientId) {
        return entryViewRepository.findOpenByClient(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public EntryResponse findById(Long id) {
        return toResponse(entryRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Entrada não encontrada.")));
    }

    @Override
    @Transactional
    public EntryResponse create(EntryRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ApiRequestException("Cliente não encontrado."));
        if (client.getStatus() != RecordStatus.ACTIVE) {
            throw new ApiRequestException("O cliente está arquivado.");
        }
        Vehicle vehicle = vehicleOf(client, request.getVehicleId());

        Entry entry = new Entry();
        entry.setNumber(documentSequenceService.nextNumber(DocumentSequenceService.TYPE_ENTRY));
        entry.setClient(client);
        entry.setClientName(client.getName());
        applyVehicle(entry, vehicle);
        entry.setEntryAt(resolveEntryAt(request.getEntryAt()));
        entry.setWorkStatus(WorkStatus.PENDING);
        entry.setResponsible(SecurityUtils.currentUsername());
        entry.setNotes(request.getNotes());
        buildLines(entry, request.getLines(), false);

        return toResponse(entryRepository.save(entry));
    }

    /**
     * Funcionarios editam trabalhos por entregar; depois da entrega, so o gestor.
     * O novo total nunca pode ficar abaixo do que ja foi recebido.
     */
    @Override
    @Transactional
    public EntryResponse update(Long id, EntryRequest request) {
        Entry entry = lock(id);
        ensureEditable(entry);
        if (!entry.getClient().getId().equals(request.getClientId())) {
            throw new ApiRequestException(
                    "Não é possível mudar o cliente de uma entrada. Cancele-a e registe uma nova.");
        }

        BigDecimal totalBefore = entry.getTotal();
        boolean categoryChanged = false;
        if (entry.getVehicle() == null || !entry.getVehicle().getId().equals(request.getVehicleId())) {
            Vehicle vehicle = vehicleOf(entry.getClient(), request.getVehicleId());
            categoryChanged = vehicle.getCategory() != entry.getVehicleCategory();
            applyVehicle(entry, vehicle);
        }
        if (request.getEntryAt() != null) {
            Instant requested = MaputoTime.toInstant(request.getEntryAt());
            if (!requested.equals(entry.getEntryAt())) {
                entry.setEntryAt(resolveEntryAt(request.getEntryAt()));
            }
        }
        entry.setNotes(request.getNotes());
        buildLines(entry, request.getLines(), categoryChanged);

        BigDecimal paid = allocationRepository.paidByEntry(id);
        if (entry.getTotal().compareTo(paid) < 0) {
            throw new ApiRequestException(String.format(
                    "O novo total (%s) é inferior ao valor já recebido (%s). Regularize os pagamentos primeiro.",
                    entry.getTotal().toPlainString(), Money.of(paid).toPlainString()));
        }
        if (totalBefore.compareTo(entry.getTotal()) != 0) {
            changeHistoryService.record(ChangeHistoryService.ENTRY, id, "UPDATE_TOTAL", null,
                    totalBefore.toPlainString(), entry.getTotal().toPlainString());
        }
        return toResponse(entryRepository.save(entry));
    }

    @Override
    @Transactional
    public EntryResponse changeStatus(Long id, StatusRequest request) {
        Entry entry = lock(id);
        WorkStatus current = entry.getWorkStatus();
        WorkStatus target = request.getWorkStatus();
        if (target == WorkStatus.CANCELLED) {
            throw new ApiRequestException("Use a opção de cancelamento, com motivo.");
        }
        if (current == WorkStatus.CANCELLED) {
            throw new ApiRequestException("A entrada está cancelada.");
        }
        if (current == target) {
            return toResponse(entry);
        }
        if (current == WorkStatus.DELIVERED) {
            SecurityUtils.require(MANAGE_DELIVERED, "Alterações depois da entrega estão reservadas ao gestor.");
            entry.setDeliveredAt(null);
            entry.setDeliveredBy(null);
        }

        if (target == WorkStatus.DELIVERED) {
            BigDecimal balance = entry.getTotal().subtract(allocationRepository.paidByEntry(id));
            // Cliente sem conta corrente: entregar com saldo pendente exige autorizacao do gestor.
            if (balance.signum() > 0 && !entry.getClient().isCreditAllowed()) {
                SecurityUtils.require("authorize:credit", String.format(
                        "Saldo pendente de %s MZN e o cliente não tem conta corrente. "
                                + "A entrega exige autorização do gestor.", Money.of(balance).toPlainString()));
                String reason = SecurityUtils.requireReason(request.getReason(),
                        "Indique o motivo da autorização de entrega com saldo pendente.");
                entry.setCreditAuthorizedBy(SecurityUtils.currentUsername());
                entry.setCreditReason(reason);
                changeHistoryService.record(ChangeHistoryService.ENTRY, id, "CREDIT_AUTHORIZATION", reason,
                        null, Money.of(balance).toPlainString());
            }
            entry.setDeliveredAt(Instant.now());
            entry.setDeliveredBy(SecurityUtils.currentUsername());
        }

        entry.setWorkStatus(target);
        changeHistoryService.record(ChangeHistoryService.ENTRY, id, "STATUS", request.getReason(), current, target);
        return toResponse(entryRepository.save(entry));
    }

    @Override
    @Transactional
    public EntryResponse cancel(Long id, CancelRequest request) {
        Entry entry = lock(id);
        if (entry.getWorkStatus() == WorkStatus.CANCELLED) {
            throw new ApiRequestException("A entrada já está cancelada.");
        }
        BigDecimal paid = allocationRepository.paidByEntry(id);
        if (paid.signum() != 0) {
            throw new ApiRequestException(String.format(
                    "A entrada tem %s MZN recebidos. Estorne os pagamentos antes de cancelar.",
                    Money.of(paid).toPlainString()));
        }
        WorkStatus before = entry.getWorkStatus();
        entry.setWorkStatus(WorkStatus.CANCELLED);
        entry.setCancelledAt(Instant.now());
        entry.setCancelledBy(SecurityUtils.currentUsername());
        entry.setCancelReason(request.getReason().trim());
        changeHistoryService.record(ChangeHistoryService.ENTRY, id, "CANCEL", request.getReason().trim(),
                before, WorkStatus.CANCELLED);
        return toResponse(entryRepository.save(entry));
    }

    @Override
    @Transactional
    public EntryResponse createOpeningBalance(OpeningBalanceRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ApiRequestException("Cliente não encontrado."));
        if (request.getDate().isAfter(MaputoTime.today())) {
            throw new ApiRequestException("A data de corte não pode ser futura.");
        }
        BigDecimal amount = Money.of(request.getAmount());

        Entry entry = new Entry();
        entry.setNumber(documentSequenceService.nextNumber(DocumentSequenceService.TYPE_ENTRY));
        entry.setClient(client);
        entry.setClientName(client.getName());
        entry.setEntryAt(MaputoTime.toInstant(request.getDate().atStartOfDay()));
        entry.setWorkStatus(WorkStatus.DELIVERED);
        entry.setOpeningBalance(true);
        entry.setResponsible(SecurityUtils.currentUsername());
        entry.setNotes(request.getNotes());
        entry.setTotal(amount);

        EntryLine line = new EntryLine();
        line.setEntry(entry);
        line.setServiceName("Saldo de abertura");
        line.setPricingType(PricingType.VARIABLE);
        line.setQuantity(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP));
        line.setUnitPrice(amount);
        line.setSubtotal(amount);
        entry.getLines().add(line);

        Entry saved = entryRepository.save(entry);
        changeHistoryService.record(ChangeHistoryService.ENTRY, saved.getId(), "OPENING_BALANCE",
                request.getNotes(), null, amount.toPlainString());
        return toResponse(saved);
    }

    // ------------------------------------------------------------------------

    private Entry lock(Long id) {
        return entryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiRequestException("Entrada não encontrada."));
    }

    private void ensureEditable(Entry entry) {
        if (entry.getWorkStatus() == WorkStatus.CANCELLED) {
            throw new ApiRequestException("A entrada está cancelada e não pode ser alterada.");
        }
        if (entry.isOpeningBalance()) {
            throw new ApiRequestException("Saldos de abertura não podem ser editados.");
        }
        if (entry.getWorkStatus() == WorkStatus.DELIVERED) {
            SecurityUtils.require(MANAGE_DELIVERED, "Alterações depois da entrega estão reservadas ao gestor.");
        }
    }

    private Vehicle vehicleOf(Client client, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ApiRequestException("Viatura não encontrada."));
        if (!vehicle.getClient().getId().equals(client.getId())) {
            throw new ApiRequestException("A viatura seleccionada não pertence a este cliente.");
        }
        if (vehicle.getStatus() != RecordStatus.ACTIVE) {
            throw new ApiRequestException("A viatura está arquivada.");
        }
        return vehicle;
    }

    private void applyVehicle(Entry entry, Vehicle vehicle) {
        entry.setVehicle(vehicle);
        entry.setVehiclePlate(vehicle.getPlate());
        entry.setVehicleDescription(vehicle.describe());
        entry.setVehicleCategory(vehicle.getCategory());
    }

    private Instant resolveEntryAt(LocalDateTime requested) {
        if (requested == null) {
            return Instant.now();
        }
        MaputoTime.validateMovementDate(requested.toLocalDate());
        return MaputoTime.toInstant(requested);
    }

    /**
     * Linhas existentes conservam o nome e o preco de catalogo do momento em que
     * foram registadas; linhas novas usam o catalogo actual.
     */
    private void buildLines(Entry entry, List<LineRequest> requests, boolean repriceAll) {
        Map<Long, EntryLine> existing = entry.getLines().stream()
                .filter(line -> line.getId() != null)
                .collect(Collectors.toMap(EntryLine::getId, Function.identity()));
        VehicleCategory category = entry.getVehicleCategory();

        List<EntryLine> lines = new ArrayList<>();
        BigDecimal total = Money.ZERO;
        int order = 0;
        for (LineRequest request : requests) {
            EntryLine kept = request.getId() == null ? null : existing.get(request.getId());
            EntryLine line;
            if (kept != null && !repriceAll && kept.getService() != null
                    && kept.getService().getId().equals(request.getServiceId())) {
                line = kept;
            } else {
                ServiceItem service = serviceItemRepository.findById(request.getServiceId())
                        .orElseThrow(() -> new ApiRequestException("Serviço não encontrado."));
                if (service.getStatus() != RecordStatus.ACTIVE) {
                    throw new ApiRequestException(String.format("O serviço \"%s\" está arquivado.", service.getName()));
                }
                line = new EntryLine();
                line.setEntry(entry);
                line.setService(service);
                line.setServiceName(service.getName());
                line.setPricingType(service.getPricingType());
                line.setCategory(category);
                BigDecimal catalogPrice = service.priceFor(category);
                line.setCatalogPrice(catalogPrice == null ? null : Money.of(catalogPrice));
            }
            priceLine(line, request);
            line.setSortOrder(order++);
            lines.add(line);
            total = total.add(line.getSubtotal());
        }

        entry.getLines().clear();
        entry.getLines().addAll(lines);
        entry.setTotal(Money.of(total));
    }

    private void priceLine(EntryLine line, LineRequest request) {
        BigDecimal quantity = request.getQuantity() == null
                ? BigDecimal.ONE
                : request.getQuantity();
        if (quantity.signum() <= 0) {
            throw new ApiRequestException(String.format("Quantidade inválida em \"%s\".", line.getServiceName()));
        }
        quantity = quantity.setScale(2, RoundingMode.HALF_UP);

        BigDecimal unitPrice;
        if (line.getPricingType() == PricingType.VARIABLE) {
            if (request.getUnitPrice() == null || request.getUnitPrice().signum() <= 0) {
                throw new ApiRequestException(String.format("Indique o preço de \"%s\".", line.getServiceName()));
            }
            unitPrice = Money.of(request.getUnitPrice());
            line.setAdjustReason(null);
        } else {
            unitPrice = request.getUnitPrice() == null ? line.getCatalogPrice() : Money.of(request.getUnitPrice());
            if (unitPrice.signum() < 0) {
                throw new ApiRequestException(String.format("Preço inválido em \"%s\".", line.getServiceName()));
            }
            boolean unchanged = line.getId() != null && line.getUnitPrice() != null
                    && unitPrice.compareTo(line.getUnitPrice()) == 0;
            boolean adjusted = unitPrice.compareTo(line.getCatalogPrice()) != 0;
            if (!adjusted) {
                line.setAdjustReason(null);
            } else if (!unchanged) {
                SecurityUtils.require("adjust:prices", String.format(
                        "Só o gestor pode ajustar o preço de \"%s\".", line.getServiceName()));
                line.setAdjustReason(SecurityUtils.requireReason(request.getAdjustReason(),
                        String.format("Indique o motivo do ajuste de preço em \"%s\".", line.getServiceName())));
            }
            // Ajustado e inalterado: o ajuste ja foi autorizado e o motivo registado mantem-se.
        }

        line.setQuantity(quantity);
        line.setUnitPrice(unitPrice);
        line.setSubtotal(Money.of(unitPrice.multiply(quantity)));
    }

    private EntryResponse toResponse(Entry entry) {
        return EntryResponse.from(entry, allocationRepository.paidByEntry(entry.getId()),
                allocationRepository.findByEntry(entry.getId()));
    }
}
