package mz.co.amaterasu.gestao.user.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(UserStatus userStatus) {
        if (userStatus == null) {
            return null;
        }
        return userStatus == UserStatus.ACTIVE ? (short) 1 : (short) 2;
    }

    @Override
    public UserStatus convertToEntityAttribute(Short code) {
        if (code == null) {
            return null;
        }
        return code == 1 ? UserStatus.ACTIVE : UserStatus.SUSPENDED;
    }
}
