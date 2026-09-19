package dev.sorokin.application.payment;

import dev.sorokin.api.payment.AuthorizePaymentRequestDto;
import dev.sorokin.api.payment.AuthorizePaymentResponseDto;
import dev.sorokin.api.payment.CapturePaymentRequestDto;
import dev.sorokin.api.warehouse.CalculatePricingRequestDto;
import dev.sorokin.domain.order.OrderEntity;
import dev.sorokin.domain.order.PaymentStatus;
import dev.sorokin.domain.task.AsyncTaskEntity;
import dev.sorokin.domain.task.ProcessStep;
import dev.sorokin.domain.task.TaskExecutionStatus;
import dev.sorokin.infrastructure.client.StubHttpClient;
import dev.sorokin.infrastructure.persistence.order.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskProcessor {

    private final OrderJpaRepository orderRepository;
    private final StubHttpClient stubHttpClient;
    private final TransactionTemplate txTemplate;

    public TaskExecutionStatus processTask(AsyncTaskEntity task) {
        UUID orderId = task.getOrderId();

        var orderLookup = orderRepository.findById(orderId);

        if (orderLookup.isEmpty()) {
            log.error("Order id {} not found", orderId);
            return TaskExecutionStatus.NON_RETRYABLE_ERROR;
        }

        var processOrder = orderLookup.get();

        task.setStep(ProcessStep.AUTH);
        log.info("Requesting authorization: orderId={}, amount={}", orderId, processOrder.getClientEstimate());
        var authorization = stubHttpClient
                .authorizePayment(
                        new AuthorizePaymentRequestDto(processOrder.getClientId(), processOrder.getClientEstimate()));

        switch (authorization.status()) {
            case AUTHORIZED -> {
                processOrder = handleAuthorizedAnswer(processOrder);
                log.info("Authorization succeeded: orderId={}, authorizedAmount={}", orderId, processOrder.getAuthorizedAmount());
            }
            case DECLINED -> {
                log.info("Authorization declined: orderId={}, reason={}", orderId, authorization.message());
                handleDeclinedAuthorization(processOrder, authorization);
                return TaskExecutionStatus.NON_RETRYABLE_ERROR;
            }
        }

        task.setStep(ProcessStep.REPRICE);
        var reprice = stubHttpClient.calculatePricing(new CalculatePricingRequestDto(orderId));
        log.info("Warehouse repricing completed: orderId={}, authorizedAmount={}, finalAmount={}",
                orderId, processOrder.getAuthorizedAmount(), reprice.finalAmount());

        if (reprice.finalAmount().compareTo(processOrder.getAuthorizedAmount()) > 0) {
            log.warn("Final price exceeds authorized amount, capture will be skipped: orderId={}, authorizedAmount={}, finalAmount={}",
                    orderId, processOrder.getAuthorizedAmount(), reprice.finalAmount());
            orderRepository.save(processOrder.toBuilder()
                    .paymentStatus(PaymentStatus.PRICE_CHANGED_FAILED)
                    .finalAmount(reprice.finalAmount())
                    .failureReason(String.format("Price has changed from %s to %s", processOrder.getAuthorizedAmount(), reprice.finalAmount()))
                    .build());
            return TaskExecutionStatus.NON_RETRYABLE_ERROR;
        }

        task.setStep(ProcessStep.CAPTURE);
        log.info("Requesting capture: orderId={}, amount={}", orderId, reprice.finalAmount());
        var capture = stubHttpClient.capturePayment(new CapturePaymentRequestDto(reprice.finalAmount(), processOrder.getClientId()));
        switch (capture.status()) {
            case CAPTURED -> {
                log.info("Capture succeeded: orderId={}, capturedAmount={}", orderId, capture.capturedAmount());
                orderRepository.save(processOrder.toBuilder()
                        .finalAmount(reprice.finalAmount())
                        .capturedAmount(capture.capturedAmount())
                        .paymentStatus(PaymentStatus.SUCCEED_PAID)
                        .build());
                return TaskExecutionStatus.SUCCESS;
            }
            case FAILED -> {
                log.warn("Capture failed: orderId={}, amount={}", orderId, reprice.finalAmount());
                orderRepository.save(processOrder.toBuilder()
                        .finalAmount(reprice.finalAmount())
                        .paymentStatus(PaymentStatus.CAPTURE_FAILED)
                        .failureReason("Capture failed")
                        .build());
                return TaskExecutionStatus.NON_RETRYABLE_ERROR;
            }
            default -> {
                log.error("Unexpected capture status: orderId={}, status={}", orderId, capture.status());
                return TaskExecutionStatus.RETRYABLE_ERROR;
            }
        }
    }

    public void handleDeclinedAuthorization(OrderEntity orderEntity, AuthorizePaymentResponseDto authorizePaymentResponseDto) {
        txTemplate.execute(status -> {
            orderRepository.save(orderEntity.toBuilder()
                    .paymentStatus(PaymentStatus.AUTHORIZATION_FAILED)
                    .failureReason(authorizePaymentResponseDto.message())
                    .build()
            );
            return orderEntity;
        });
    }

    public OrderEntity handleAuthorizedAnswer(OrderEntity orderEntity) {
        return txTemplate.execute(status -> orderRepository.save(orderEntity.toBuilder()
                .authorizedAmount(orderEntity.getClientEstimate())
                .build()
        ));

    }
}
