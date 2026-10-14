package mz.co.amaterasu.gestao.user.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private String accessToken;

    private long expiresIn;

    public LoginResponse setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public LoginResponse setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "accessToken='" + accessToken + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
