package dev.sorokin.domain.order;

import dev.sorokin.utils.EnumUtils;

public enum PaymentStatus implements EnumUtils.IntEnum {
    NEW(0),
    AUTHORIZATION_FAILED(1),
    PRICE_CHANGED_FAILED(2),
    SUCCEED_PAID(3),
    CAPTURE_FAILED(4);

    private final int value;

    PaymentStatus(int value) {
        this.value = value;
    }


    @Override
    public int getCode() {
        return value;
    }
}
