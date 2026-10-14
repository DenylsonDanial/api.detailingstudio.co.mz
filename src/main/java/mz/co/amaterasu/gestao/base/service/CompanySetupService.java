package mz.co.amaterasu.gestao.base.service;

import mz.co.amaterasu.gestao.base.dto.CompanySetupDTO;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public interface CompanySetupService {

    void companySetup(CompanySetupDTO companySetupDTO) throws ApiRequestException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, SQLException, IOException;

}
