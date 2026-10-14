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
@Table(name = "tb_provinces")
@NoArgsConstructor
@AllArgsConstructor
public class Province implements Serializable {

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
    @OneToMany(mappedBy = "province")
    private Set<District> districts;
    @ManyToOne @Getter @Setter
    @JoinColumn(name = "country_id")
    private Country country;
    @Getter @Setter @Column(name = "created_at")
    private Date createdAt;
    @Getter @Setter @Column(name = "created_by")
    private Long createdBy;
}
