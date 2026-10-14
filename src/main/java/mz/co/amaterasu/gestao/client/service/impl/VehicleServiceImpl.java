package mz.co.amaterasu.gestao.client.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.base.service.ChangeHistoryService;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.ReassignRequest;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.VehicleRequest;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.VehicleResponse;
import mz.co.amaterasu.gestao.client.entity.Client;
import mz.co.amaterasu.gestao.client.entity.Vehicle;
import mz.co.amaterasu.gestao.client.repository.ClientRepository;
import mz.co.amaterasu.gestao.client.repository.VehicleRepository;
import mz.co.amaterasu.gestao.client.service.VehicleService;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;
    private final ChangeHistoryService changeHistoryService;

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findAll(Pageable pageable, RecordStatus status, Long clientId, String term) {
        Set<RecordStatus> statuses = status == null ? EnumSet.allOf(RecordStatus.class) : EnumSet.of(status);
        String clean = term == null ? "" : term.trim();
        String like = clean.isEmpty() ? "%" : "%" + clean.toLowerCase() + "%";
        String plate = clean.isEmpty() ? "%" : "%" + Vehicle.normalizePlate(clean) + "%";
        return vehicleRepository.search(statuses, clientId == null ? 0L : clientId, like, plate, pageable)
                .map(VehicleResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) {
        return VehicleResponse.from(get(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehicleResponse> findActiveByPlate(String plate) {
        return vehicleRepository.findFirstByPlateNormalizedAndStatus(Vehicle.normalizePlate(plate), RecordStatus.ACTIVE)
                .map(VehicleResponse::from);
    }

    @Override
    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setClient(activeClient(request.getClientId()));
        apply(vehicle, request);
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    /** O proprietario nao muda aqui: a reassociacao e reservada ao gestor. */
    @Override
    @Transactional
    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = get(id);
        apply(vehicle, request);
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    /**
     * Muda o proprietario actual. As entradas antigas mantem o cliente da data do
     * registo, por isso servicos e dividas anteriores nao sao transferidos.
     */
    @Override
    @Transactional
    public VehicleResponse reassign(Long id, ReassignRequest request) {
        Vehicle vehicle = get(id);
        Client target = activeClient(request.getClientId());
        Client previous = vehicle.getClient();
        if (previous.getId().equals(target.getId())) {
            throw new ApiRequestException("A viatura já pertence a este cliente.");
        }
        vehicle.setClient(target);
        changeHistoryService.record(ChangeHistoryService.VEHICLE, id, "REASSIGN", request.getReason().trim(),
                previous.getId() + " - " + previous.getName(), target.getId() + " - " + target.getName());
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleResponse setStatus(Long id, RecordStatus status) {
        Vehicle vehicle = get(id);
        if (status == RecordStatus.ACTIVE) {
            ensurePlateAvailable(vehicle.getPlateNormalized(), id);
        }
        RecordStatus before = vehicle.getStatus();
        vehicle.setStatus(status);
        changeHistoryService.record(ChangeHistoryService.VEHICLE, id,
                status == RecordStatus.ARCHIVED ? "ARCHIVE" : "UNARCHIVE", null, before, status);
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    private Vehicle get(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Viatura não encontrada."));
    }

    private Client activeClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ApiRequestException("Cliente não encontrado."));
        if (client.getStatus() != RecordStatus.ACTIVE) {
            throw new ApiRequestException("O cliente está arquivado.");
        }
        return client;
    }

    private void apply(Vehicle vehicle, VehicleRequest request) {
        String normalized = Vehicle.normalizePlate(request.getPlate());
        if (normalized.isEmpty()) {
            throw new ApiRequestException("A matrícula é obrigatória.");
        }
        if (vehicle.getStatus() == RecordStatus.ACTIVE) {
            ensurePlateAvailable(normalized, vehicle.getId());
        }
        vehicle.setPlate(request.getPlate().trim().toUpperCase().replaceAll("\\s+", " "));
        vehicle.setPlateNormalized(normalized);
        vehicle.setBrand(blankToNull(request.getBrand()));
        vehicle.setModel(blankToNull(request.getModel()));
        vehicle.setCategory(request.getCategory());
        vehicle.setNotes(request.getNotes());
    }

    private void ensurePlateAvailable(String normalized, Long ownId) {
        vehicleRepository.findFirstByPlateNormalizedAndStatus(normalized, RecordStatus.ACTIVE)
                .filter(existing -> !existing.getId().equals(ownId))
                .ifPresent(existing -> {
                    throw new ApiRequestException(String.format(
                            "A matrícula %s já está registada para o cliente %s.",
                            existing.getPlate(), existing.getClient().getName()));
                });
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
