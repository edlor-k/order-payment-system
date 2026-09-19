package dev.sorokin.api.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.sorokin.domain.order.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderDto(
        UUID id,
        String address,
        BigDecimal clientEstimate,
        BigDecimal authorizedAmount,
        BigDecimal finalAmount,
        BigDecimal capturedAmount,
        PaymentStatus paymentStatus,
        String failureReason
) { }
