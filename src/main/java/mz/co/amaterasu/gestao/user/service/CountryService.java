package mz.co.amaterasu.gestao.user.service;

import mz.co.amaterasu.gestao.user.dto.CountryDTO;

import java.util.Set;

public interface CountryService {

    Set<CountryDTO> listCountries();

    Set<CountryDTO> listCountriesByDescription(String description);
}
