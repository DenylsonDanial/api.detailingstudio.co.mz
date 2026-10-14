package mz.co.amaterasu.gestao.user.resource;

import mz.co.amaterasu.gestao.user.dto.ProvinceDTO;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface ProvinceResource {

    ResponseEntity<Set<ProvinceDTO>> findAllProvinces();
    ResponseEntity<Set<ProvinceDTO>> findProvincesByCountry(Long countryId);
    ResponseEntity<Set<ProvinceDTO>> findBySlug(String slug);
}
