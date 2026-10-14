package mz.co.amaterasu.gestao.user.repository;

import mz.co.amaterasu.gestao.user.dto.CountryDTO;
import mz.co.amaterasu.gestao.user.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query("select c from Country c where " +
            "lower( c.slug ) like %:slug%")
    Set<CountryDTO> findBySlug(@Param("slug") String slug);

    @Query("select c from Country c")
    Set<CountryDTO> findAllCountries();
}
