package mz.co.amaterasu.gestao.user.repository;

import mz.co.amaterasu.gestao.user.dto.PermissionDTO;
import mz.co.amaterasu.gestao.user.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
	Permission findByName(String name);

	List<Permission> findAllByContextPath(String contextPath);

	@Query("SELECT p FROM Permission p WHERE " +
			"LOWER(p.name) LIKE LOWER(CONCAT('%', LOWER(:searchTerm), '%')) OR " +
			"LOWER(CAST(p.displayName AS string)) LIKE LOWER(CONCAT('%', LOWER(:searchTerm), '%')) OR " +
			"LOWER(p.bundler) LIKE LOWER(CONCAT('%', LOWER(:searchTerm), '%')) ")
	Page<PermissionDTO> findAllPaginated(@Param("searchTerm") String searchTerm, Pageable pageable);

}
