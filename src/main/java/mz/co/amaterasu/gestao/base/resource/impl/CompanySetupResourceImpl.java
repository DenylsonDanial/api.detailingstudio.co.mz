package mz.co.amaterasu.gestao.base.resource.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.dto.CompanySetupDTO;
import mz.co.amaterasu.gestao.base.dto.TenantDTO;
import mz.co.amaterasu.gestao.base.resource.CompanySetupResource;
import mz.co.amaterasu.gestao.base.service.CompanySetupService;
import mz.co.amaterasu.gestao.base.service.TenantService;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/companies")
public class CompanySetupResourceImpl implements CompanySetupResource {

    private final CompanySetupService setupService;
    private final TenantService tenantService;


    @PreAuthorize("hasAuthority('create:companies')")
    @PostMapping("setup")
    @Override
    public void companySetup(@RequestBody CompanySetupDTO companySetupDTO) throws ApiRequestException, SQLException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        setupService.companySetup(companySetupDTO);
    }

    @PreAuthorize("hasAuthority('read:companies')")
    @GetMapping(value = "/list")
    @Override
    public Page<TenantDTO> findAllTenantsByName(@RequestParam(name = "term", required = false) String term, Pageable pageable) {
        return tenantService.findAllTenantsByName(term, pageable);
    }

    @PreAuthorize("hasAuthority('update:companies')")
    @GetMapping(value = "/activate/{id}")
    @Override
    public void activateTenant(@PathVariable long id) throws ApiRequestException {
        tenantService.activateTenant(id);
    }

    @PreAuthorize("hasAuthority('update:companies')")
    @GetMapping(value = "/deactivate/{id}")
    @Override
    public void deactivateTenant(@PathVariable long id) throws ApiRequestException {
        tenantService.deactivateTenant(id);
    }
}
