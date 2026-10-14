package mz.co.amaterasu.gestao.user.service;

import jakarta.transaction.Transactional;
import mz.co.amaterasu.gestao.user.annotaion.MasterDB;
import mz.co.amaterasu.gestao.user.dto.RequestSuccessResponse;
import mz.co.amaterasu.gestao.user.dto.RoleRequest;
import mz.co.amaterasu.gestao.user.dto.projection.PRoleDTO;
import mz.co.amaterasu.gestao.user.entity.Permission;
import mz.co.amaterasu.gestao.user.entity.Role;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.user.repository.PermissionRepository;
import mz.co.amaterasu.gestao.user.repository.RoleRepository;
import mz.co.amaterasu.gestao.util.RequestsUtil;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {

	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	private final Environment environment;

	public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, Environment environment) {
		this.roleRepository = roleRepository;
		this.permissionRepository = permissionRepository;
		this.environment = environment;
	}

	@MasterDB
	public PRoleDTO insert(RoleRequest roleRequest) throws ApiRequestException
	{
		Boolean roleByName = roleRepository.selectExistsName(roleRequest.name());
		Boolean roleByDisplayName = roleRepository.selectExistsDisplayName(roleRequest.displayName());
		if (roleByName || roleByDisplayName) {
			throw new ApiRequestException(environment.getProperty("exists.role"));
		}
		Role role = new Role();
		role.setName(roleRequest.name());
		role.setDisplayName(roleRequest.displayName());
		if (!roleRequest.permissions().isEmpty()) {
			Set<Long> checkedIds = RequestsUtil.idsChecker(roleRequest.permissions());
			role.getPermissions().addAll(findAllByIds(checkedIds));
		}
		Role savedRole = roleRepository.save(role);
		return new PRoleDTO(savedRole);
	}

	@MasterDB
	public PRoleDTO findById(Long id) throws ApiRequestException {
		if (id == null) {
			throw new ApiRequestException(environment.getProperty("notfound.role"));
		}
		Optional<Role> role = roleRepository.findById(id);
		return new PRoleDTO(role.orElse(new Role()));
	}

	@MasterDB
	public Page<Role> findAll(Pageable pageable) {
		return roleRepository.findAll(pageable);
	}

	@MasterDB
	public RequestSuccessResponse delete(Long id) throws ApiRequestException {
		Optional<Role> role = roleRepository.findById(id);
		if (role.isEmpty()) {
			throw new ApiRequestException(environment.getProperty("notfound.role"));
		}

		RequestSuccessResponse resp = new RequestSuccessResponse(
				String.format("%s ", environment.getProperty("msg.deleted")),
				HttpStatus.OK);
		roleRepository.deleteById(id);
		return  resp;
	}

	@MasterDB
	public PRoleDTO update(Long id, RoleRequest roleRequest) throws ApiRequestException {
		Set<Long> checkedIds = RequestsUtil.idsChecker(roleRequest.permissions());
		if (checkedIds.isEmpty()) {
			throw new ApiRequestException(environment.getProperty("notnull.permissions"));
		}
		Optional<Role> role = roleRepository.findById(id);
		if (role.isEmpty()) {
			throw new ApiRequestException(environment.getProperty("notfound.role"));
		}
		role.get().setName(roleRequest.name());
		role.get().setDisplayName(roleRequest.displayName());
		role.get().getPermissions().clear();
		role.get().getPermissions().addAll(findAllByIds(checkedIds));
		return new PRoleDTO(roleRepository.save(role.get()));
	}

	@MasterDB
	public Set<Permission> findAllByIds(Set<Long> ids) {
		Set<Permission> permissions = new HashSet<>();
		ids.stream().map(p -> {
			Optional<Permission> permission = permissionRepository.findById(p);
			permission.ifPresent(permissions::add);
			return permission;
		}).collect(Collectors.toSet());
		return permissions;
	}
}
