package dev.sorokin.domain.task;

import dev.sorokin.utils.EnumUtils;

public enum ProcessStep implements EnumUtils.IntEnum{
    NEW(0),
    AUTH(1),
    REPRICE(2),
    CAPTURE(3),;

    private final int value;

    ProcessStep(int value) {
        this.value = value;
    }

    @Override
    public int getCode() {
        return value;
    }
}
