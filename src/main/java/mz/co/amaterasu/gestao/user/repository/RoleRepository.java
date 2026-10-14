package mz.co.amaterasu.gestao.user.repository;

import mz.co.amaterasu.gestao.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(String name);

	@Query("" +
			"SELECT CASE WHEN COUNT(r) > 0 THEN " +
			"TRUE ELSE FALSE END " +
			"FROM Role r " +
			"WHERE r.name = ?1"
	)
	Boolean selectExistsName(String name);

	@Query("" +
			"SELECT CASE WHEN COUNT(r) > 0 THEN " +
			"TRUE ELSE FALSE END " +
			"FROM Role r " +
			"WHERE r.displayName = ?1"
	)
	Boolean selectExistsDisplayName(String displayName);

}
