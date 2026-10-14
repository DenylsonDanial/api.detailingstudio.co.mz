package mz.co.amaterasu.gestao.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "tb_districts")
@NoArgsConstructor
@AllArgsConstructor
public class District implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;
    @Getter @Setter
    private String description;
    @Getter @Setter
    private String slug;
    @Getter @Setter
    @ManyToOne
    @JoinColumn(name = "province_id", referencedColumnName = "id")
    private Province province;
    @Getter @Setter @Column(name = "created_at")
    private Date createdAt;
    @Getter @Setter @Column(name = "created_by")
    private Long createdBy;
}
