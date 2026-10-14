package mz.co.amaterasu.gestao.user.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LoginUserDTO {
    private String username;
    private String password;
    private String tenantId;

}
