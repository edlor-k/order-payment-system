package dev.sorokin.infrastructure.persistence.task;

import dev.sorokin.domain.task.TaskStatus;
import dev.sorokin.utils.EnumUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TaskStatusConverter implements AttributeConverter<TaskStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(TaskStatus attribute) {
        return attribute != null
                ? attribute.getCode()
                : null;
    }

    @Override
    public TaskStatus convertToEntityAttribute(Integer dbData) {
        return dbData != null
                ? EnumUtils.fromCode(TaskStatus.class, dbData)
                : null;
    }
}
