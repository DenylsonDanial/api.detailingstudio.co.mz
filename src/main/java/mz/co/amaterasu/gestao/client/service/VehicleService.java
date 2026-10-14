package mz.co.amaterasu.gestao.client.service;

import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.ReassignRequest;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.VehicleRequest;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface VehicleService {

    Page<VehicleResponse> findAll(Pageable pageable, RecordStatus status, Long clientId, String term);

    VehicleResponse findById(Long id);

    /** Viatura activa com a mesma matricula normalizada, se existir. */
    Optional<VehicleResponse> findActiveByPlate(String plate);

    VehicleResponse create(VehicleRequest request);

    VehicleResponse update(Long id, VehicleRequest request);

    VehicleResponse reassign(Long id, ReassignRequest request);

    VehicleResponse setStatus(Long id, RecordStatus status);
}
