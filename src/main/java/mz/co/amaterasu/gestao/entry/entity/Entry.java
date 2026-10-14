package mz.co.amaterasu.gestao.entry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.client.entity.Client;
import mz.co.amaterasu.gestao.client.entity.Vehicle;
import mz.co.amaterasu.gestao.client.enums.VehicleCategory;
import mz.co.amaterasu.gestao.entry.enums.WorkStatus;
import mz.co.amaterasu.gestao.util.Auditable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entrada de viatura (atendimento). Guarda o cliente, a viatura e os valores do
 * momento do registo; o valor pago e calculado pelas afectacoes de pagamentos.
 */
@Entity
@Table(name = "tb_entries")
@Getter
@Setter
@NoArgsConstructor
public class Entry extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, updatable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "vehicle_plate")
    private String vehiclePlate;

    @Column(name = "vehicle_description")
    private String vehicleDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_category")
    private VehicleCategory vehicleCategory;

    @Column(name = "entry_at", nullable = false)
    private Instant entryAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status", nullable = false)
    private WorkStatus workStatus = WorkStatus.PENDING;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(name = "opening_balance", nullable = false, updatable = false)
    private boolean openingBalance;

    @Column(nullable = false)
    private String responsible;

    private String notes;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "delivered_by")
    private String deliveredBy;

    @Column(name = "credit_authorized_by")
    private String creditAuthorizedBy;

    @Column(name = "credit_reason")
    private String creditReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancelled_by")
    private String cancelledBy;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<EntryLine> lines = new ArrayList<>();
}
