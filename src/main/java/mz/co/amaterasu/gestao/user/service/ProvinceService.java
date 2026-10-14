package mz.co.amaterasu.gestao.user.service;

import mz.co.amaterasu.gestao.user.dto.ProvinceDTO;

import java.util.Set;

public interface ProvinceService {
    Set<ProvinceDTO> findAllProvinces();
    Set<ProvinceDTO> findProvincesByCountry(Long countryId);
    Set<ProvinceDTO> findBySlug(String slug);
}
