package mz.co.amaterasu.gestao.base.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String username;
    private String name;
    private String tenantId;
    private List<String> authorities;

    /** Verdadeiro quando o utilizador nunca definiu a palavra-passe e tem de a definir agora. */
    private boolean mustChangePassword;
}
