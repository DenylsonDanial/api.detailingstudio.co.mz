package mz.co.amaterasu.gestao.base.dto;

import lombok.Data;
import mz.co.amaterasu.gestao.user.dto.UserDTO;

@Data
public class CompanySetupDTO {

    private UserDTO  user;
    private BusinessSettingDTO businessSetting;
    private long userId;
}
