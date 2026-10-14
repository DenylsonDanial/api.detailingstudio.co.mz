package mz.co.amaterasu.gestao.base.repository;

import mz.co.amaterasu.gestao.base.dto.TenantDTO;
import mz.co.amaterasu.gestao.base.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    boolean existsBySlug(String slug);

    @Query("select new mz.co.amaterasu.gestao.base.dto.TenantDTO(t.id, t.slug, t.tenantName, t.databaseUrl, t.databaseUsername, t.databasePassword, t.isActive) from Tenant t where lower(t.tenantName) like concat('%', lower(:name), '%')")
    Page<TenantDTO> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("select new mz.co.amaterasu.gestao.base.dto.TenantDTO(t.id, t.slug, t.tenantName, t.databaseUrl, t.databaseUsername, t.databasePassword, t.isActive) from Tenant t")
    Page<TenantDTO> findAllDTO(Pageable pageable);
}
