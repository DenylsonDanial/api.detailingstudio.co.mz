package mz.co.amaterasu.gestao.user.service.impl;

import lombok.AllArgsConstructor;
import mz.co.amaterasu.gestao.user.annotaion.MasterDB;
import mz.co.amaterasu.gestao.user.dto.DistrictDTO;
import mz.co.amaterasu.gestao.user.repository.DistrictRepository;
import mz.co.amaterasu.gestao.user.service.DistrictService;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private DistrictRepository repository;

    @MasterDB
    @Override
    public Set<DistrictDTO> findAllDistricts() {
        return repository.findAllDistricts();
    }

    @MasterDB
    @Override
    public Set<DistrictDTO> findDistrictsByProvince(Long provinceId) {
        return repository.findDistrictsByProvince(provinceId);
    }

    @MasterDB
    @Override
    public Set<DistrictDTO> findBySlug(String slug) {
        return repository.findBySlug(slug);
    }
}
