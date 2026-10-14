package mz.co.amaterasu.gestao.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "tb_countries")
@NoArgsConstructor
@AllArgsConstructor
public class Country implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;
    @Getter @Setter
    private String description;
    @Getter @Setter
    private String slug;
    @Getter @Setter
    private String symbol;
    @Getter @Setter
    @OneToMany(mappedBy = "country")
    private Set<Province> provinces;
    @Getter @Setter @Column(name = "created_at")
    private Date createdAt;
    @Getter @Setter @Column(name = "created_by")
    private Long createdBy;
}
