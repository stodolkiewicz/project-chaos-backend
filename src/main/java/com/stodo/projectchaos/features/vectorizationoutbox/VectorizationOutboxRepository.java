package com.stodo.projectchaos.features.vectorizationoutbox;

import com.stodo.projectchaos.model.entity.VectorizationOutboxEntity;
import com.stodo.projectchaos.model.enums.VectorizationOutboxStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VectorizationOutboxRepository extends JpaRepository<VectorizationOutboxEntity, UUID> {

    @Query("SELECT v FROM VectorizationOutboxEntity v WHERE v.status = :status AND v.retryCount < :maxRetries")
    List<VectorizationOutboxEntity> findPendingWithRetryCountLessThan(
            @Param("status") VectorizationOutboxStatusEnum status,
            @Param("maxRetries") int maxRetries
    );
}
