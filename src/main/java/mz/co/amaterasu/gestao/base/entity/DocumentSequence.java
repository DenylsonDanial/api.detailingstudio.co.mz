package mz.co.amaterasu.gestao.base.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_document_sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_type", nullable = false, length = 30)
    private String documentType;

    @Column(nullable = false, length = 10)
    private String prefix;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "last_number", nullable = false)
    @Builder.Default
    private Integer lastNumber = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer padding = 4;
}
