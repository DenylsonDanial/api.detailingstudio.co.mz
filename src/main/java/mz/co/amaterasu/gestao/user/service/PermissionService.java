package mz.co.amaterasu.gestao.user.service;

import mz.co.amaterasu.gestao.user.annotaion.MasterDB;
import mz.co.amaterasu.gestao.user.dto.PermissionDTO;
import mz.co.amaterasu.gestao.user.entity.Permission;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.user.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class PermissionService {

	public static final String SLASH = "/";
	@Autowired
	private PermissionRepository permissionRepository;

	@MasterDB
	public PermissionDTO insert(PermissionDTO dto) {
		String contextPath = dto.getContextPath().startsWith(SLASH) ? dto.getContextPath() : SLASH +dto.getContextPath();
		Permission p = permissionRepository.save(new Permission(null, dto.getName(), dto.getDisplayName(), contextPath,dto.getBundler()));
		return new PermissionDTO(p);
	}

	@MasterDB
	public Permission findById(Long id) throws ApiRequestException {
		return permissionRepository.findById(id)
				.orElseThrow(() -> new ApiRequestException("Not found"));
	}

	@MasterDB
	public Permission findByName(String name) {
		return permissionRepository.findByName(name);
	}

	@MasterDB
	public List<String> findByContextPath(String contextPath) {
		return permissionRepository.findAllByContextPath(contextPath)
				.stream().map(Permission::getName).toList();
	}

	@MasterDB
	public List<Permission> findAll() {
		return permissionRepository.findAll();
	}

	@MasterDB
	public Page<PermissionDTO> findAllPaginatedFilter(String term, Pageable pageable) {
		if (term == null) {
			return permissionRepository.findAll(pageable).map(PermissionDTO::new);
		} else {
			return permissionRepository.findAllPaginated(term, pageable);
		}
	}

	@MasterDB
	public void delete(Long id) {
		permissionRepository.deleteById(id);
	}

	@MasterDB
	public Permission update(Long id, Permission permission) throws ApiRequestException {
		Permission p = permissionRepository.findById(id)
				.orElseThrow(() -> new ApiRequestException("Not found"));
		p.setName(permission.getName());
		p.setDisplayName(permission.getDisplayName());
		p.setBundler(permission.getBundler());
		String contextPath = permission.getContextPath().toLowerCase(Locale.ROOT);
		if (!contextPath.startsWith("/")) {
			contextPath = "/" + contextPath;
		}
		p.setContextPath(contextPath);
		permissionRepository.save(p);
		return p;
	}
}
