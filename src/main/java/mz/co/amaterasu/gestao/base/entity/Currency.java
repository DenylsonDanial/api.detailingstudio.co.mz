package mz.co.amaterasu.gestao.base.entity;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "tb_currencies")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Currency implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;
    @Getter @Setter
    private String name;
    @Getter @Setter @Column(name = "short_name")
    private String shortName;
    @Getter @Setter
    private String symbol;
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at")
    @Temporal(TemporalType.DATE)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.DATE)
    private Date updatedAt;

}
