package mz.co.amaterasu.gestao.user.resource.impl;

import mz.co.amaterasu.gestao.user.dto.DistrictDTO;
import mz.co.amaterasu.gestao.user.resource.DistrictResource;
import mz.co.amaterasu.gestao.user.service.DistrictService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("districts")
public class DistrictResourceImpl implements DistrictResource {

    private final DistrictService service;

    public DistrictResourceImpl(DistrictService service) {
        this.service = service;
    }

    @Override
    @GetMapping("findAll")
    public ResponseEntity<Set<DistrictDTO>> findAllDistricts() {
        return new ResponseEntity<>(service.findAllDistricts(), HttpStatus.OK);
    }

    @Override
    @GetMapping("findDistrictsByProvince/{provinceId}")
    public ResponseEntity<Set<DistrictDTO>> findDistrictsByProvince(@PathVariable Long provinceId) {
        return new ResponseEntity<>(service.findDistrictsByProvince(provinceId), HttpStatus.OK);
    }

    @Override
    @GetMapping("searchBySlug")
    public ResponseEntity<Set<DistrictDTO>> findBySlug(@RequestParam(name = "slug") String slug) {
        return new ResponseEntity<>(service.findBySlug(slug), HttpStatus.OK);
    }
}
