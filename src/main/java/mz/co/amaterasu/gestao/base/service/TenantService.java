package mz.co.amaterasu.gestao.base.service;

import mz.co.amaterasu.gestao.base.dto.TenantDTO;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TenantService {

    Page<TenantDTO> findAllTenantsByName(String name, Pageable pageable);
    void activateTenant(long id) throws ApiRequestException;
    void deactivateTenant(long id) throws ApiRequestException;
}
