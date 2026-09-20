package dev.sorokin.domain.task;

import dev.sorokin.infrastructure.persistence.task.ProcessStepConverter;
import dev.sorokin.infrastructure.persistence.task.TaskStatusConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder(toBuilder = true)
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "order_id")
    private UUID orderId;

    @Convert(converter = TaskStatusConverter.class)
    @Column(name = "task_status")
    private TaskStatus status;

    @Column(name = "process_step")
    @Convert(converter = ProcessStepConverter.class)
    private ProcessStep step;

    @Column(name = "attempts")
    private Integer attempts;

    @Column(name = "next_attempt_at")
    private OffsetDateTime nextAttemptAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}