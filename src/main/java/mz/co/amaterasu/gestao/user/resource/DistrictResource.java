package mz.co.amaterasu.gestao.user.resource;

import mz.co.amaterasu.gestao.user.dto.DistrictDTO;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface DistrictResource {

    ResponseEntity<Set<DistrictDTO>> findAllDistricts();
    ResponseEntity<Set<DistrictDTO>> findDistrictsByProvince(Long countryId);
    ResponseEntity<Set<DistrictDTO>> findBySlug(String slug);
}
