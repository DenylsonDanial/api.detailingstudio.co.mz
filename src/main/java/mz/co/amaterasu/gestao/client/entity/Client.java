package mz.co.amaterasu.gestao.client.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.client.enums.ClientType;
import mz.co.amaterasu.gestao.util.Auditable;

@Entity
@Table(name = "tb_clients")
@Getter
@Setter
@NoArgsConstructor
public class Client extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false)
    private ClientType clientType = ClientType.INDIVIDUAL;

    private String phone;

    @Column(name = "credit_allowed", nullable = false)
    private boolean creditAllowed;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE;
}
