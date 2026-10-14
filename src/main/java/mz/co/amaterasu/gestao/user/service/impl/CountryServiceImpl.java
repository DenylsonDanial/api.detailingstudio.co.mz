package mz.co.amaterasu.gestao.user.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.user.annotaion.MasterDB;
import mz.co.amaterasu.gestao.user.dto.CountryDTO;
import mz.co.amaterasu.gestao.user.repository.CountryRepository;
import mz.co.amaterasu.gestao.user.service.CountryService;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryRepository repository;

    @MasterDB
    @Override
    public Set<CountryDTO> listCountries() {
        return repository.findAllCountries();
    }

    @MasterDB
    @Override
    public Set<CountryDTO> listCountriesByDescription(String slug) {
        return repository.findBySlug(slug);
    }
}
