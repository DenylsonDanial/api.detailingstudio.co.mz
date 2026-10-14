package mz.co.amaterasu.gestao.user.enums;

import lombok.Getter;

@Getter
public enum Gender {
	F("Femenino"), M("Masculino");

	private final String code;

	Gender(String code) {
        this.code = code;
    }


}
