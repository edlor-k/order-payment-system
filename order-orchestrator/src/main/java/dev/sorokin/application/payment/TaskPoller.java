package dev.sorokin.application.payment;

import dev.sorokin.domain.task.AsyncTaskEntity;
import dev.sorokin.domain.task.TaskStatus;
import dev.sorokin.infrastructure.persistence.task.TaskJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskPoller {

    private final TransactionTemplate txTemplate;
    private final TaskJpaRepository taskJpaRepository;
    private final TaskPollerProperties props;
    private final TaskDispatcher taskDispatcher;

    @Scheduled(fixedRate = 5000)
    public void poll() {
        log.info("Polling task started");
        List<AsyncTaskEntity> taskBatch = pickTasksForBatchSize();

        var taskIds = taskBatch.stream()
                .map(AsyncTaskEntity::getId)
                .toList();
        log.info("Successfully picked tasks: taskIds = {}", taskIds);

        if (taskBatch.isEmpty()) {
            return;
        }

        for (AsyncTaskEntity task :  taskBatch) {
            taskDispatcher.dispatch(task);
        }

    }

    private List<AsyncTaskEntity> pickTasksForBatchSize() {
        return txTemplate.execute(status ->  {
            List<AsyncTaskEntity> tasks = taskJpaRepository.pickBatchForProcessing(
                    TaskStatus.NEW.getCode(),
                    TaskStatus.FAILED_RETRYABLE.getCode(),
                    OffsetDateTime.now(),
                    props.getBatchSize()
            );

            var nextProcessTime = OffsetDateTime.now().plus(props.getRetryDelay());

            for (AsyncTaskEntity task : tasks) {
                task.setStatus(TaskStatus.IN_PROGRESS);
                var attempts = task.getAttempts() == null
                        ? 1 : task.getAttempts() + 1;
                task.setAttempts(attempts);
                task.setNextAttemptAt(nextProcessTime);
            }
            taskJpaRepository.saveAll(tasks);
            return tasks;
        });
    }
}