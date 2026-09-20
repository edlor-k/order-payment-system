package dev.sorokin.infrastructure.persistence.order;

import dev.sorokin.domain.order.PaymentStatus;
import dev.sorokin.utils.EnumUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(PaymentStatus attribute) {
        return attribute != null
                ? attribute.getCode()
                : null;
    }

    @Override
    public PaymentStatus convertToEntityAttribute(Integer dbData) {
        return dbData != null
                ? EnumUtils.fromCode(PaymentStatus.class, dbData)
                : null;
    }
}
