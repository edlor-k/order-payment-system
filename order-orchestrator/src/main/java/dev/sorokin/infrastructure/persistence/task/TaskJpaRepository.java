package dev.sorokin.infrastructure.persistence.task;

import dev.sorokin.domain.task.AsyncTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface TaskJpaRepository extends JpaRepository<AsyncTaskEntity, Long> {

    @Query(value = """
            select * from tasks
            where task_status = :newStatus
            or (task_status = :retryStatus and next_attempt_at <= :now)
            order by id
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<AsyncTaskEntity> pickBatchForProcessing(
            @Param("newStatus") int newStatus,
            @Param("retryStatus") int retryStatus,
            @Param("now") OffsetDateTime now,
            @Param("batchSize") int batchSize
    );
}
