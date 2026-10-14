package mz.co.amaterasu.gestao.user.repository;


import mz.co.amaterasu.gestao.user.dto.ProvinceDTO;
import mz.co.amaterasu.gestao.user.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {

    @Query("select p from Province p")
    Set<ProvinceDTO> findAllProvinces();

    @Query("select p from Province p where p.country.id = :countryId")
    Set<ProvinceDTO> findProvincesByCountry(@Param("countryId") Long countryId);

    @Query("select p from Province p where " +
            "lower( p.slug ) like %:slug%")
    Set<ProvinceDTO> findBySlug(@Param("slug") String slug);
}
