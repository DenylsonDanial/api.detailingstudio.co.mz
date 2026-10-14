package mz.co.amaterasu.gestao.base.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Token de refresh rotativo. Guarda-se apenas o hash SHA-256 do valor entregue
 * ao cliente, para que uma leitura da base de dados nao permita personificar
 * um utilizador.
 */
@Entity
@Table(name = "tb_refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at")
    private Instant createdAt;

    /** Empresa activa desta sessao; usada ao reemitir o access token no refresh.
     * Nao pode chamar-se tenantId: o Hibernate 6 trata esse nome como
     * discriminador de tenant e gravaria null (ver Tenant.slug). */
    @Column(name = "tenant_id")
    private String activeTenant;

    public boolean isUsable() {
        return revokedAt == null && expiresAt.isAfter(Instant.now());
    }
}
