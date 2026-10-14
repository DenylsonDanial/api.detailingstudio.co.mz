package mz.co.amaterasu.gestao.entry.service;

import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.CancelRequest;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryFilter;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryRequest;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.EntryResponse;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.OpeningBalanceRequest;
import mz.co.amaterasu.gestao.entry.dto.EntryDTOs.StatusRequest;
import mz.co.amaterasu.gestao.entry.entity.EntryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EntryService {

    Page<EntryView> findAll(EntryFilter filter, Pageable pageable);

    List<EntryView> findOpenByClient(Long clientId);

    EntryResponse findById(Long id);

    EntryResponse create(EntryRequest request);

    EntryResponse update(Long id, EntryRequest request);

    EntryResponse changeStatus(Long id, StatusRequest request);

    EntryResponse cancel(Long id, CancelRequest request);

    /** Saldo de abertura aprovado pelo gestor; nao conta como servico nem como recebimento. */
    EntryResponse createOpeningBalance(OpeningBalanceRequest request);
}
