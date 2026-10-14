package mz.co.amaterasu.gestao.user.service.impl;

import mz.co.amaterasu.gestao.user.annotaion.MasterDB;
import mz.co.amaterasu.gestao.user.dto.ProvinceDTO;
import mz.co.amaterasu.gestao.user.repository.ProvinceRepository;
import mz.co.amaterasu.gestao.user.service.ProvinceService;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceRepository repository;

    public ProvinceServiceImpl(ProvinceRepository repository) {
        this.repository = repository;
    }

    @MasterDB
    @Override
    public Set<ProvinceDTO> findAllProvinces() {
        return repository.findAllProvinces();
    }

    @MasterDB
    @Override
    public Set<ProvinceDTO> findProvincesByCountry(Long countryID) {
        return repository.findProvincesByCountry(countryID);
    }

    @MasterDB
    @Override
    public Set<ProvinceDTO> findBySlug(String slug) {
        return repository.findBySlug(slug);
    }
}
