package mz.co.amaterasu.gestao.base.resource;

import mz.co.amaterasu.gestao.base.dto.CompanySetupDTO;
import mz.co.amaterasu.gestao.base.dto.TenantDTO;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public interface CompanySetupResource {

    void companySetup(CompanySetupDTO companySetupDTO) throws ApiRequestException, SQLException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    Page<TenantDTO> findAllTenantsByName(String name, Pageable pageable);
    void activateTenant(long id) throws ApiRequestException;
    void deactivateTenant(long id) throws ApiRequestException;
}
