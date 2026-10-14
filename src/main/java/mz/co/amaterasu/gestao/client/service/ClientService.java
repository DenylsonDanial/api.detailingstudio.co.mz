package mz.co.amaterasu.gestao.client.service;

import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.ClientRequest;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.ClientResponse;
import mz.co.amaterasu.gestao.client.dto.ClientDTOs.CreditRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {

    Page<ClientResponse> findAll(Pageable pageable, RecordStatus status, String term);

    ClientResponse findById(Long id);

    /** Clientes com o mesmo contacto, para alertar possiveis duplicados sem os impedir. */
    List<ClientResponse> findPossibleDuplicates(String phone, Long excludeId);

    ClientResponse create(ClientRequest request);

    ClientResponse update(Long id, ClientRequest request);

    ClientResponse updateCredit(Long id, CreditRequest request);

    ClientResponse setStatus(Long id, RecordStatus status);
}
