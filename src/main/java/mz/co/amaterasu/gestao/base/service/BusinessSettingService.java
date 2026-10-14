package mz.co.amaterasu.gestao.base.service;

import mz.co.amaterasu.gestao.base.dto.BusinessSettingDTO;
import mz.co.amaterasu.gestao.base.entity.Currency;

import java.util.List;

/** Definicoes da empresa activa. Vive na base de dados do tenant. */
public interface BusinessSettingService {

    /** Definicoes do tenant corrente. Ha exactamente um registo por tenant. */
    BusinessSettingDTO findFirst();

    /**
     * Cria o registo unico de definicoes do tenant. So e chamado durante o
     * provisionamento da empresa, com o {@code TenantContext} ja apontado a base
     * de dados nova.
     */
    BusinessSettingDTO create(BusinessSettingDTO businessSettingDTO);

    BusinessSettingDTO update(BusinessSettingDTO businessSettingDTO);

    List<Currency> listCurrencies();
}
