package com.stodo.projectchaos.embedding;

import com.stodo.projectchaos.model.entity.AttachmentEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentEmbeddingRepository extends JpaRepository<AttachmentEmbeddingEntity, UUID> {
    @Query("SELECT e FROM AttachmentEmbeddingEntity e WHERE e.project.id = :projectId")
    List<AttachmentEmbeddingEntity> findByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT e FROM AttachmentEmbeddingEntity e WHERE e.task.id = :taskId")
    List<AttachmentEmbeddingEntity> findByTaskId(@Param("taskId") UUID taskId);
}