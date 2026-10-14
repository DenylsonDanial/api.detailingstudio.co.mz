package mz.co.amaterasu.gestao.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.amaterasu.gestao.user.entity.District;

import java.sql.Date;

@Data @NoArgsConstructor @AllArgsConstructor
public class DistrictDTO {

    private Long id;
    private String description;
    private String slug;
    private Date createdAt;
    private Long createdBy;

    public DistrictDTO(District district) {
        this.id = district.getId();
        this.description = district.getDescription();
        this.slug = district.getSlug();
    }
}
