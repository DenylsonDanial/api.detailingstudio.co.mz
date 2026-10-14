package mz.co.amaterasu.gestao.user.resource;

import jakarta.validation.Valid;
import mz.co.amaterasu.gestao.user.dto.PermissionDTO;
import mz.co.amaterasu.gestao.user.entity.Permission;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.user.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

	@Autowired
	private PermissionService service;

	@PreAuthorize("hasAuthority('read:permissions')")
	@GetMapping("/findAll")
	public ResponseEntity<List<Permission>> findAll() {
		List<Permission> list = service.findAll();
		return ResponseEntity.ok().body(list);
	}

	@PreAuthorize("hasAuthority('read:permissions')")
	@GetMapping("/list")
	public ResponseEntity<Page<PermissionDTO>> findAllPaginated(@RequestParam(required = false) String term, Pageable pageable) {
		Page<PermissionDTO> list = service.findAllPaginatedFilter(term, pageable);
		return ResponseEntity.ok().body(list);
	}

	@PreAuthorize("hasAuthority('read:permissions')")
	@GetMapping(value="/findById/{id}")
	public ResponseEntity<Permission> findById(@PathVariable Long id) throws ApiRequestException {
		Permission obj = service.findById(id);
		return ResponseEntity.ok().body(obj);
	}

	@PreAuthorize("hasAuthority('read:permissions')")
	@GetMapping(value="/findByContextPath")
	public ResponseEntity<List<String>> findAllByContextPath(@RequestParam(name = "contextPath") String contextPath) {
		List<String> obj = service.findByContextPath(contextPath);
		return ResponseEntity.ok().body(obj);
	}

	@PreAuthorize("hasAuthority('create:permissions')")
	@PostMapping("/insert")
	public ResponseEntity<PermissionDTO> insert(@RequestBody @Valid PermissionDTO permission) {
		permission = service.insert(permission);

		return ResponseEntity.ok(permission);
	}

	@PreAuthorize("hasAuthority('delete:permissions')")
	@DeleteMapping(value="/deleteById/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAuthority('update:permissions')")
	@PutMapping(value="/update/{id}")
	public ResponseEntity<Permission> update(@PathVariable Long id, @RequestBody Permission obj) throws ApiRequestException {
		obj = service.update(id, obj);
		return ResponseEntity.ok().body(obj);
	}
}
