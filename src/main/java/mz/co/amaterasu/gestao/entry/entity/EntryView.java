package mz.co.amaterasu.gestao.entry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import mz.co.amaterasu.gestao.client.enums.VehicleCategory;
import mz.co.amaterasu.gestao.entry.enums.EntryPaymentStatus;
import mz.co.amaterasu.gestao.entry.enums.WorkStatus;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Leitura da vista vw_entries: entrada com valor pago, saldo e estado de pagamento calculados. */
@Entity
@Immutable
@Table(name = "vw_entries")
@Getter
public class EntryView {

    @Id
    private Long id;

    private String number;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "vehicle_plate")
    private String vehiclePlate;

    @Column(name = "vehicle_description")
    private String vehicleDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_category")
    private VehicleCategory vehicleCategory;

    @Column(name = "entry_at")
    private Instant entryAt;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status")
    private WorkStatus workStatus;

    private BigDecimal total;

    @Column(name = "opening_balance")
    private boolean openingBalance;

    private String responsible;

    private String notes;

    private BigDecimal paid;

    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private EntryPaymentStatus paymentStatus;
}
