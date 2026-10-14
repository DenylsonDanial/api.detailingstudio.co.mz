package mz.co.amaterasu.gestao.user.resource.impl;

import mz.co.amaterasu.gestao.user.dto.CountryDTO;
import mz.co.amaterasu.gestao.user.resource.CountryResource;
import mz.co.amaterasu.gestao.user.service.CountryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("countries")
public class CountryResourceImpl implements CountryResource {

    private final CountryService service;

    public CountryResourceImpl(CountryService service) {
        this.service = service;
    }

    @Override
    @GetMapping("findAll")
    public ResponseEntity<Set<CountryDTO>> listCountries() {
        return new ResponseEntity<>(service.listCountries(), HttpStatus.OK);
    }

    @Override
    @GetMapping("/findCountriesBySlug")
    public ResponseEntity<Set<CountryDTO>> listCountriesByDescription(@RequestParam("slug") String slug) {
        return new ResponseEntity<>(service.listCountriesByDescription(slug), HttpStatus.OK);
    }
}
