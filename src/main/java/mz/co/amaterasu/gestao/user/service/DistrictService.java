package mz.co.amaterasu.gestao.user.service;

import mz.co.amaterasu.gestao.user.dto.DistrictDTO;

import java.util.Set;

public interface DistrictService {

    Set<DistrictDTO> findAllDistricts();
    Set<DistrictDTO> findDistrictsByProvince(Long provinceId);
    Set<DistrictDTO> findBySlug(String slug);
}
