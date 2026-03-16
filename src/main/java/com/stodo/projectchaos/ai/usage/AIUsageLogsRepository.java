package com.stodo.projectchaos.ai.usage;

import com.stodo.projectchaos.model.entity.AIUsageLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface AIUsageLogsRepository extends JpaRepository<AIUsageLogsEntity, UUID> {

    @Query("""
        SELECT COUNT(l) >= :numberOfRequestsInTimeWindow 
        FROM AIUsageLogsEntity l 
        WHERE l.user.id = :userId 
        AND l.createdDate >= :timeWindowStartDate
    """)
    boolean isNumberOfRequestsInTimeWindowLimitReached(
            UUID userId,
            int numberOfRequestsInTimeWindow,
            Instant timeWindowStartDate
    );
}

