package dev.sorokin.api.order;

import java.math.BigDecimal;

public record OrderCreateRequestDto(
        String address,
        Long clientId,
        BigDecimal clientAmount
) { }
