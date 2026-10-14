package mz.co.amaterasu.gestao.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.amaterasu.gestao.user.entity.Country;

import java.sql.Date;

@Data @NoArgsConstructor @AllArgsConstructor
public class CountryDTO {

    private Long id;
    private String description;
    private String slug;
    private String symbol;
    private Date createdAt;
    private Long createdBy;

    public CountryDTO(Country country) {
        this.id = country.getId();
        this.description = country.getDescription();
        this.slug = country.getSlug();
        this.symbol = country.getSymbol();
    }
}
