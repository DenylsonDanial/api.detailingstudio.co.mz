package mz.co.amaterasu.gestao.client.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.base.service.ChangeHistoryService;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.ClientRequest;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.ClientResponse;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.CreditRequest;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.VehicleResponse;
import mz.co.amaterasu.gestao.client.entity.Client;
import mz.co.amaterasu.gestao.client.entity.Vehicle;
import mz.co.amaterasu.gestao.client.enums.ClientType;
import mz.co.amaterasu.gestao.client.repository.ClientRepository;
import mz.co.amaterasu.gestao.client.repository.VehicleRepository;
import mz.co.amaterasu.gestao.client.service.ClientService;
import mz.co.amaterasu.gestao.entry.repository.EntryViewRepository;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.payment.repository.PaymentRepository;
import mz.co.amaterasu.gestao.util.Money;
import mz.co.amaterasu.gestao.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final EntryViewRepository entryViewRepository;
    private final PaymentRepository paymentRepository;
    private final ChangeHistoryService changeHistoryService;

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> findAll(Pageable pageable, RecordStatus status, String term) {
        Set<RecordStatus> statuses = status == null ? EnumSet.allOf(RecordStatus.class) : EnumSet.of(status);
        String clean = term == null ? "" : term.trim();
        String like = clean.isEmpty() ? "%" : "%" + clean.toLowerCase() + "%";
        String plate = clean.isEmpty() ? "%" : "%" + Vehicle.normalizePlate(clean) + "%";

        Page<ClientResponse> page = clientRepository.search(statuses, like, plate, pageable).map(ClientResponse::from);
        List<Long> ids = page.getContent().stream().map(ClientResponse::getId).toList();
        if (ids.isEmpty()) {
            return page;
        }

        Map<Long, List<VehicleResponse>> vehicles = vehicleRepository
                .findByClientIdInAndStatus(ids, RecordStatus.ACTIVE).stream()
                .map(VehicleResponse::from)
                .collect(Collectors.groupingBy(VehicleResponse::getClientId));
        Map<Long, BigDecimal> debts = new HashMap<>();
        for (Object[] row : entryViewRepository.debtByClients(ids)) {
            debts.put((Long) row[0], Money.of((BigDecimal) row[1]));
        }
        page.getContent().forEach(client -> {
            client.setVehicles(vehicles.getOrDefault(client.getId(), List.of()));
            client.setCurrentDebt(debts.getOrDefault(client.getId(), Money.ZERO));
        });
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        Client client = get(id);
        ClientResponse response = ClientResponse.from(client);
        response.setVehicles(vehicleRepository.findByClientIdOrderByStatusAscPlateAsc(id).stream()
                .map(VehicleResponse::from).toList());
        response.setCurrentDebt(Money.of(entryViewRepository.debtByClient(id)));
        response.setTotalServices(Money.of(entryViewRepository.totalServicesByClient(id)));
        response.setTotalReceived(Money.of(paymentRepository.totalReceivedByClient(id)));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> findPossibleDuplicates(String phone, Long excludeId) {
        String digits = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        if (digits.length() < 6) {
            return List.of();
        }
        return clientRepository.findByPhoneDigits(digits, excludeId == null ? 0L : excludeId).stream()
                .map(ClientResponse::from).toList();
    }

    @Override
    @Transactional
    public ClientResponse create(ClientRequest request) {
        boolean credit = Boolean.TRUE.equals(request.getCreditAllowed());
        if (credit) {
            SecurityUtils.require("manage:credit", "Só o gestor pode autorizar conta corrente.");
        }
        Client client = new Client();
        apply(client, request);
        client.setCreditAllowed(credit);
        Client saved = clientRepository.save(client);
        if (credit) {
            changeHistoryService.record(ChangeHistoryService.CLIENT, saved.getId(), "CREDIT", null, false, true);
        }
        return ClientResponse.from(saved);
    }

    /** A autorizacao de conta corrente nao muda aqui: tem endpoint e permissao proprios. */
    @Override
    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = get(id);
        apply(client, request);
        return ClientResponse.from(clientRepository.save(client));
    }

    /**
     * Desactivar impede novo credito, mas as dividas anteriores continuam
     * visiveis e liquidaveis.
     */
    @Override
    @Transactional
    public ClientResponse updateCredit(Long id, CreditRequest request) {
        Client client = get(id);
        boolean before = client.isCreditAllowed();
        boolean after = request.getCreditAllowed();
        if (before != after) {
            client.setCreditAllowed(after);
            changeHistoryService.record(ChangeHistoryService.CLIENT, id, "CREDIT", request.getReason(), before, after);
        }
        return ClientResponse.from(clientRepository.save(client));
    }

    @Override
    @Transactional
    public ClientResponse setStatus(Long id, RecordStatus status) {
        Client client = get(id);
        RecordStatus before = client.getStatus();
        client.setStatus(status);
        changeHistoryService.record(ChangeHistoryService.CLIENT, id,
                status == RecordStatus.ARCHIVED ? "ARCHIVE" : "UNARCHIVE", null, before, status);
        return ClientResponse.from(clientRepository.save(client));
    }

    private Client get(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Cliente não encontrado."));
    }

    private void apply(Client client, ClientRequest request) {
        client.setName(request.getName().trim());
        client.setClientType(request.getClientType() == null ? ClientType.INDIVIDUAL : request.getClientType());
        client.setPhone(request.getPhone() == null || request.getPhone().isBlank() ? null : request.getPhone().trim());
        client.setNotes(request.getNotes());
    }
}
