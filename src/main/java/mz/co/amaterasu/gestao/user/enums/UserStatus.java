package mz.co.amaterasu.gestao.user.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    SUSPENDED("suspended"), ACTIVE("active");

    private final String code;

    UserStatus(String code) {
        this.code = code;
    }


}
