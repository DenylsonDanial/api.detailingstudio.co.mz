package mz.co.amaterasu.gestao.user.resource.impl;

import mz.co.amaterasu.gestao.user.dto.ProvinceDTO;
import mz.co.amaterasu.gestao.user.resource.ProvinceResource;
import mz.co.amaterasu.gestao.user.service.ProvinceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("provinces")
public class ProvinceResourceImpl implements ProvinceResource {

    private final ProvinceService service;

    public ProvinceResourceImpl(ProvinceService service) {
        this.service = service;
    }

    @Override
    @GetMapping("findAll")
    public ResponseEntity<Set<ProvinceDTO>> findAllProvinces() {
        return new ResponseEntity<>(service.findAllProvinces(), HttpStatus.OK);
    }

    @Override
    @GetMapping("findByCountryId/{countryId}")
    public ResponseEntity<Set<ProvinceDTO>> findProvincesByCountry(@PathVariable Long countryId) {
        return new ResponseEntity<>(service.findProvincesByCountry(countryId), HttpStatus.OK);
    }

    @Override
    @GetMapping("searchBySlug")
    public ResponseEntity<Set<ProvinceDTO>> findBySlug(@RequestParam(name = "slug") String slug) {
        return new ResponseEntity<>(service.findBySlug(slug), HttpStatus.OK);
    }
}
