package mz.co.amaterasu.gestao.base.service.impl;

import mz.co.amaterasu.gestao.base.dto.TenantDTO;
import mz.co.amaterasu.gestao.base.entity.Tenant;
import mz.co.amaterasu.gestao.base.repository.TenantRepository;
import mz.co.amaterasu.gestao.base.service.TenantService;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantServiceImpl implements TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Override
    public Page<TenantDTO> findAllTenantsByName(String name, Pageable pageable) {
        if (StringUtils.isNotBlank(name)) {
            return tenantRepository.findAllByNameContainingIgnoreCase(name, pageable);
        }
        return tenantRepository.findAllDTO(pageable);
    }

    @Override
    public void activateTenant(long id) throws ApiRequestException {
        Tenant tenantOptional = tenantRepository.
                findById(id).orElseThrow(() -> new ApiRequestException("Empresa não encontrada"));

        tenantOptional.setActive(true);
        tenantRepository.save(tenantOptional);
    }

    @Override
    public void deactivateTenant(long id) throws ApiRequestException {
        Tenant tenantOptional = tenantRepository
                .findById(id).orElseThrow(() -> new ApiRequestException("Empresa não encontrada"));

        tenantOptional.setActive(false);
        tenantRepository.save(tenantOptional);
    }
}
