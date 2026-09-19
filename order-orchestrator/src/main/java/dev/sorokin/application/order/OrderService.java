package dev.sorokin.application.order;

import dev.sorokin.api.order.OrderCreateRequestDto;
import dev.sorokin.domain.order.OrderEntity;
import dev.sorokin.domain.order.PaymentStatus;
import dev.sorokin.domain.task.AsyncTaskEntity;
import dev.sorokin.domain.task.ProcessStep;
import dev.sorokin.domain.task.TaskStatus;
import dev.sorokin.infrastructure.persistence.order.OrderJpaRepository;
import dev.sorokin.infrastructure.persistence.task.TaskJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderJpaRepository orderRepository;
    private final TaskJpaRepository taskRepository;
    private final TransactionTemplate txTemplate;

    public OrderEntity createOrder(
            OrderCreateRequestDto requestDto
    ) {
        return txTemplate.execute((status) -> {
            var entity = OrderEntity.builder()
                    .address(requestDto.address())
                    .clientId(requestDto.clientId())
                    .clientEstimate(requestDto.clientAmount())
                    .paymentStatus(PaymentStatus.NEW)
                    .build();
            orderRepository.save(entity);
            var task = AsyncTaskEntity.builder()
                    .orderId(entity.getId())
                    .status(TaskStatus.NEW)
                    .step(ProcessStep.NEW)
                    .build();
            taskRepository.save(task);
            log.info("Order created and payment task enqueued: orderId={}, taskId={}", entity.getId(), task.getId());
            return entity;
        });
    }

    public Optional<OrderEntity> findOrder(UUID id) {
        return orderRepository.findById(id);
    }
}
