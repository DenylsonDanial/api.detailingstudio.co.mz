package mz.co.amaterasu.gestao.base.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Agrupa os payloads de entrada do modulo de autenticacao. */
public final class AuthRequests {

    private AuthRequests() {
    }

    @Data
    public static class Login {
        @NotBlank(message = "O utilizador e obrigatorio")
        private String username;

        @NotBlank(message = "A palavra-passe e obrigatoria")
        private String password;

        /** Opcional: permite entrar directamente numa empresa especifica. */
        private String tenantId;
    }

    @Data
    public static class Refresh {
        @NotBlank
        private String refreshToken;

        /** Empresa activa a preservar nesta rotacao; validada em tb_user_tenants. */
        private String tenantId;
    }

    @Data
    public static class SwitchCompany {
        @NotBlank(message = "A empresa e obrigatoria")
        private String tenantId;

        @NotBlank(message = "A sessao e obrigatoria")
        private String refreshToken;
    }

    @Data
    public static class ForgotPassword {
        @NotBlank
        @Email(message = "Endereco de e-mail invalido")
        private String email;
    }

    @Data
    public static class ResetPassword {
        @NotBlank
        private String token;

        @NotBlank
        @Size(min = 8, message = "A palavra-passe deve ter pelo menos 8 caracteres")
        private String password;

        @NotBlank
        private String passwordConfirm;
    }

    @Data
    public static class ChangePassword {
        @NotBlank
        private String currentPassword;

        @NotBlank
        @Size(min = 8, message = "A palavra-passe deve ter pelo menos 8 caracteres")
        private String password;

        @NotBlank
        private String passwordConfirm;
    }

    /** Primeiro acesso: troca a palavra-passe temporaria pela definitiva. */
    @Data
    public static class SetPassword {
        @NotBlank
        @Size(min = 8, message = "A palavra-passe deve ter pelo menos 8 caracteres")
        private String password;

        @NotBlank
        private String passwordConfirm;
    }
}
