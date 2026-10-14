package mz.co.amaterasu.gestao.base.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.dto.BusinessSettingDTO;
import mz.co.amaterasu.gestao.base.entity.BusinessSetting;
import mz.co.amaterasu.gestao.base.entity.Currency;
import mz.co.amaterasu.gestao.base.repository.BusinessSettingRepository;
import mz.co.amaterasu.gestao.base.repository.CurrencyRepository;
import mz.co.amaterasu.gestao.base.service.BusinessSettingService;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.util.GenericMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessSettingServiceImpl implements BusinessSettingService {

    private final BusinessSettingRepository businessSettingRepository;
    private final CurrencyRepository currencyRepository;

    @Override
    @Transactional(readOnly = true)
    public BusinessSettingDTO findFirst() {
        BusinessSetting entity = businessSettingRepository.findByIsActive(true)
                .orElseThrow(() -> new ApiRequestException("Definicoes da empresa nao encontradas."));

        return toDto(entity);
    }

    @Override
    @Transactional
    public BusinessSettingDTO create(BusinessSettingDTO dto) {
        if (businessSettingRepository.count() > 0) {
            throw new ApiRequestException("Esta empresa ja tem definicoes criadas.");
        }

        BusinessSetting entity = new BusinessSetting();
        entity.setBusinessName(dto.getBusinessName());
        entity.setNuit(dto.getNuit());
        entity.setEmail(dto.getEmail());
        entity.setAddress(dto.getAddress());
        entity.setWebsite(dto.getWebsite());
        entity.setLogo(dto.getLogo());
        entity.setPhone(dto.getPhone());
        entity.setAltPhone(dto.getAltPhone());
        entity.setProvince(dto.getProvince());
        entity.setCity(dto.getCity());
        entity.setActivitySector(dto.getActivitySector());
        entity.setDbTenantName(dto.getDbTenantName());
        entity.setDefaultTaxRate(dto.getDefaultTaxRate() != null
                ? dto.getDefaultTaxRate()
                : java.math.BigDecimal.valueOf(16));
        entity.setActivityStart(dto.getActivityStart());
        entity.setActive(true);
        entity.setCreatedAt(new Date());
        entity.setCurrency(resolveCurrency(dto));

        return toDto(businessSettingRepository.save(entity));
    }

    @Override
    @Transactional
    public BusinessSettingDTO update(BusinessSettingDTO dto) {
        BusinessSetting entity = businessSettingRepository.findByIsActive(true)
                .orElseThrow(() -> new ApiRequestException("Definicoes da empresa nao encontradas."));

        entity.setBusinessName(dto.getBusinessName());
        entity.setNuit(dto.getNuit());
        entity.setEmail(dto.getEmail());
        entity.setAddress(dto.getAddress());
        entity.setWebsite(dto.getWebsite());
        entity.setPhone(dto.getPhone());
        entity.setAltPhone(dto.getAltPhone());
        entity.setProvince(dto.getProvince());
        entity.setCity(dto.getCity());
        entity.setActivitySector(dto.getActivitySector());
        entity.setActivityStart(dto.getActivityStart());
        if (dto.getDefaultTaxRate() != null) {
            entity.setDefaultTaxRate(dto.getDefaultTaxRate());
        }
        entity.setUpdatedAt(new Date());

        // O logo so e substituido quando vem no pedido, para nao o apagar em
        // actualizacoes parciais feitas noutros separadores das definicoes.
        if (dto.getLogo() != null && !dto.getLogo().isBlank()) {
            entity.setLogo(dto.getLogo());
        }

        Currency currency = resolveCurrency(dto);
        if (currency != null) {
            entity.setCurrency(currency);
        }

        return toDto(businessSettingRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Currency> listCurrencies() {
        return currencyRepository.findAll();
    }

    /**
     * O DTO aceita a moeda por id (escrita) ou pelo objecto completo (quando vem
     * de um payload de leitura reaproveitado). Em ambos os casos e carregada da
     * base de dados: nunca se persiste a instancia recebida do cliente.
     */
    private Currency resolveCurrency(BusinessSettingDTO dto) {
        Long currencyId = dto.getCurrencyId();
        if (currencyId == null && dto.getCurrency() != null) {
            currencyId = dto.getCurrency().getId();
        }
        if (currencyId == null) {
            return null;
        }
        return currencyRepository.findById(currencyId)
                .orElseThrow(() -> new ApiRequestException("Moeda inexistente."));
    }

    private BusinessSettingDTO toDto(BusinessSetting entity) {
        BusinessSettingDTO dto = GenericMapper.map(entity, BusinessSettingDTO.class);
        if (entity.getCurrency() != null) {
            dto.setCurrencyId(entity.getCurrency().getId());
        }
        return dto;
    }
}
