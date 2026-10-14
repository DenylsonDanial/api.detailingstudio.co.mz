package mz.co.amaterasu.gestao.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.amaterasu.gestao.user.entity.Province;

import java.sql.Date;

@Data @AllArgsConstructor @NoArgsConstructor
public class ProvinceDTO {

    private Long id;
    private String description;
    private String slug;
    private Date createdAt;
    private Long createdBy;

    public ProvinceDTO(Province province) {
        this.id = province.getId();
        this.description = province.getDescription();
        this.slug = province.getSlug();
    }
}
