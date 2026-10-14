package mz.co.amaterasu.gestao.user.repository;


import mz.co.amaterasu.gestao.base.dto.TenantDTO;
import mz.co.amaterasu.gestao.user.entity.User;
import mz.co.amaterasu.gestao.base.entity.UserTenant;
import mz.co.amaterasu.gestao.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import mz.co.amaterasu.gestao.user.dto.projection.PUserFindAllDTO;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

	Optional<User> findByEmailIgnoreCase(String email);

	@Query("select ut from UserTenant ut inner join Tenant t on ut.tenant = t.slug " +
			"where ut.username = :username and t.isActive")
	List<UserTenant> findAllUserTenantsByUsername(String username);

	@Query("select new mz.co.amaterasu.gestao.base.dto.TenantDTO(t.id, t.slug, t.tenantName, t.databaseUrl, t.databaseUsername, t.databasePassword, t.isActive) from Tenant t inner join UserTenant ut on t.slug = ut.tenant  where ut.username = :username")
	List<TenantDTO> findAllTenantsByUsername(String username);

	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
	boolean existsByNuit(Long phone);


	@Query("SELECT u FROM User u WHERE " +
			"(:userStatus is null OR u.userStatus = :userStatus) " +
			"AND (:userName is null OR LOWER(u.name) LIKE LOWER(concat('%', :userName, '%')))")
	Page<PUserFindAllDTO> findAllPaginated(@Param("userStatus") UserStatus userStatus,
										   @Param("userName") String userName, Pageable pageable);

}
