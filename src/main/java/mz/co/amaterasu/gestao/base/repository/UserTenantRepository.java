package mz.co.amaterasu.gestao.base.repository;

import mz.co.amaterasu.gestao.base.entity.UserTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTenantRepository extends JpaRepository<UserTenant, Long> {

    @Modifying
    @Query("delete from UserTenant ut where ut.username = :username")
    void deleteUserTenantsByUsername(String username);
}
