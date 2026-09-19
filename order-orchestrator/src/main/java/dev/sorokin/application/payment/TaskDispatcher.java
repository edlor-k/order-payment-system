package dev.sorokin.application.payment;

import dev.sorokin.domain.task.AsyncTaskEntity;
import dev.sorokin.domain.task.TaskExecutionStatus;
import dev.sorokin.domain.task.TaskStatus;
import dev.sorokin.infrastructure.persistence.task.TaskJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskDispatcher {

    private final TaskProcessor taskProcessor;
    private final TaskJpaRepository taskRepository;
    private final TaskDispatcherProperties taskDispatcherProperties;

    public void dispatch(AsyncTaskEntity task) {
        CompletableFuture
                .supplyAsync(() -> taskProcessor.processTask(task))
                .thenAccept(result -> handleTaskExecuted(task, result))
                .exceptionally(ex -> handleExceptionInTaskHappened(task, ex)
                );
    }

    private Void handleExceptionInTaskHappened(AsyncTaskEntity task, Throwable ex) {
        log.error("Task failed with unexpected exception:", ex);
        scheduleTaskRetry(task);
        return null;
    }

    private void handleTaskExecuted(AsyncTaskEntity task, TaskExecutionStatus result) {
        log.info("Task executed, taskId = {}, status = {}", task.getId(), result);
        switch (result) {
            case SUCCESS ->  handleTaskSucceeded(task);
            case RETRYABLE_ERROR -> scheduleTaskRetry(task);
            case NON_RETRYABLE_ERROR -> handleTaskFailed(task);
        }
    }

    private void handleTaskFailed(AsyncTaskEntity task) {
        log.warn("Task finished as non-retryable failure: taskId = {}", task.getId());
        saveTaskWithStatus(task, TaskStatus.FAILED_NON_RETRYABLE);
    }

    private void handleTaskSucceeded(AsyncTaskEntity task) {
        log.info("Task succeeded, taskId = {}", task.getId());
        saveTaskWithStatus(task, TaskStatus.SUCCEEDED);
    }

    private void scheduleTaskRetry(AsyncTaskEntity task) {
        log.info("Scheduling task retry for taskId = {}", task.getId());
        if (task.getAttempts() >= taskDispatcherProperties.getMaxAttempts()) {
            log.error("Maximum number of tasks reached for taskId = {}", task.getId());
            saveTaskWithStatus(task, TaskStatus.FAILED_NON_RETRYABLE);
            return;
        }

        var nextAttemptAt = OffsetDateTime.now().plus(taskDispatcherProperties.getRetryDelay());
        taskRepository.save(task.toBuilder()
                .nextAttemptAt(nextAttemptAt)
                .status(TaskStatus.FAILED_RETRYABLE)
                .build());
    }

    private void saveTaskWithStatus(AsyncTaskEntity task, TaskStatus status) {
        taskRepository.save(task.toBuilder()
                .status(status)
                .nextAttemptAt(null)
                .build());
    }
}
