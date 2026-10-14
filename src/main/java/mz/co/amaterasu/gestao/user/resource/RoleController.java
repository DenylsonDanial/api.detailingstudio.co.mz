package mz.co.amaterasu.gestao.user.resource;

import jakarta.validation.Valid;
import mz.co.amaterasu.gestao.user.dto.RequestSuccessResponse;
import mz.co.amaterasu.gestao.user.dto.RoleRequest;
import mz.co.amaterasu.gestao.user.dto.projection.PRoleDTO;
import mz.co.amaterasu.gestao.user.entity.Role;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.user.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
public class RoleController {

	@Autowired
	private RoleService service;

	@PreAuthorize("hasAuthority('read:roles')")
	@GetMapping("/findAll")
	public ResponseEntity<Page<Role>> findAll(Pageable pageable) {
		Page<Role> list = service.findAll(pageable);
		return ResponseEntity.ok().body(list);
	}

	@PreAuthorize("hasAuthority('read:roles')")
	@GetMapping(value="/findById/{id}")
	public ResponseEntity<PRoleDTO> findById(@PathVariable Long id) throws ApiRequestException {
			PRoleDTO obj = service.findById(id);
			return new ResponseEntity<>(obj, HttpStatus.OK);
	}

	@PreAuthorize("hasAuthority('create:roles')")
	@PostMapping("/insert")
	public ResponseEntity<PRoleDTO> insert(@Valid @RequestBody RoleRequest roleRequest) throws ApiRequestException {
		PRoleDTO role = service.insert(roleRequest);

		return ResponseEntity.ok().body(role);
	}

	@PreAuthorize("hasAuthority('delete:roles')")
	@DeleteMapping(value="/deleteById/{id}")
	public ResponseEntity<RequestSuccessResponse> delete(@PathVariable Long id) throws ApiRequestException {
		RequestSuccessResponse result = service.delete(id);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PreAuthorize("hasAuthority('update:roles')")
	@PutMapping(value="/update/{id}")
	public ResponseEntity<PRoleDTO> update(@PathVariable Long id, @RequestBody RoleRequest roleRequest) throws ApiRequestException {
		PRoleDTO role = service.update(id, roleRequest);
		return ResponseEntity.ok().body(role);
	}
}
