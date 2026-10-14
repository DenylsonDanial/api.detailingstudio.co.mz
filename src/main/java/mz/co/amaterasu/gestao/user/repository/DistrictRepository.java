package mz.co.amaterasu.gestao.user.repository;

import mz.co.amaterasu.gestao.user.dto.DistrictDTO;
import mz.co.amaterasu.gestao.user.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    @Query("select d from District d")
    Set<DistrictDTO> findAllDistricts();

    @Query("select d from District d where d.province.id = :provinceId")
    Set<DistrictDTO> findDistrictsByProvince(@Param("provinceId") Long provinceId);

    @Query("select d from District d where " +
            "lower( d.slug ) like %:slug%")
    Set<DistrictDTO> findBySlug(@Param("slug") String slug);
}
