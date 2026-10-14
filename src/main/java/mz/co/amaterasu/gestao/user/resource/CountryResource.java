package mz.co.amaterasu.gestao.user.resource;

import mz.co.amaterasu.gestao.user.dto.CountryDTO;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface CountryResource {

    ResponseEntity<Set<CountryDTO>> listCountries();

    ResponseEntity<Set<CountryDTO>> listCountriesByDescription(String description);
}
