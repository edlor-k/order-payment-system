package dev.sorokin.infrastructure.persistence.task;

import dev.sorokin.domain.task.ProcessStep;
import dev.sorokin.utils.EnumUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProcessStepConverter implements AttributeConverter<ProcessStep, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ProcessStep attribute) {
        return attribute != null
                ? attribute.getCode()
                : null;
    }

    @Override
    public ProcessStep convertToEntityAttribute(Integer dbData) {
        return dbData != null
                ? EnumUtils.fromCode(ProcessStep.class, dbData)
                : null;
    }
}
