package mz.co.amaterasu.gestao.client.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.client.enums.VehicleCategory;
import mz.co.amaterasu.gestao.util.Auditable;

@Entity
@Table(name = "tb_vehicles")
@Getter
@Setter
@NoArgsConstructor
public class Vehicle extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private String plate;

    @Column(name = "plate_normalized", nullable = false)
    private String plateNormalized;

    private String brand;

    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleCategory category;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE;

    /** Ex.: "Toyota Hilux (ABC 123 MC)". */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        if (brand != null && !brand.isBlank()) {
            sb.append(brand.trim());
        }
        if (model != null && !model.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(model.trim());
        }
        return sb.isEmpty() ? plate : sb + " (" + plate + ")";
    }

    /** Remove espacos, hifenes e pontos e passa a maiusculas, para detectar duplicados. */
    public static String normalizePlate(String plate) {
        return plate == null ? "" : plate.replaceAll("[\\s\\-.]", "").toUpperCase();
    }
}
