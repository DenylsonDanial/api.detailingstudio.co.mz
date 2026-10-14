package mz.co.amaterasu.gestao.user.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, Short> {

    @Override
    public Short convertToDatabaseColumn(Gender gender) {
        if (gender == null) {
            return null;
        }
        return gender == Gender.M ? (short) 1 : (short) 2;
    }

    @Override
    public Gender convertToEntityAttribute(Short code) {
        if (code == null) {
            return null;
        }
        return code == 1 ? Gender.M : Gender.F;
    }
}
