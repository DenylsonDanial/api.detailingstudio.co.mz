package mz.co.amaterasu.gestao.user.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordSetDTO {

    @NotBlank
    @Size(min = 8, message = "A palavra-passe deve ter pelo menos 8 caracteres")
    private String password;
    @NotBlank
    private String passwordConfirm;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}
