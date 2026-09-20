package dev.sorokin.domain.task;

import dev.sorokin.utils.EnumUtils;

public enum TaskExecutionStatus implements EnumUtils.StringEnum {
    SUCCESS("SUCCESS"),
    RETRYABLE_ERROR("RETRYABLE_ERROR"),
    NON_RETRYABLE_ERROR("NON_RETRYABLE_ERROR"),;

    private final String value;

    TaskExecutionStatus(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
