package com.stodo.projectchaos.features.attachment;

import com.stodo.projectchaos.model.entity.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, UUID> {

    @Modifying
    @Query("DELETE FROM AttachmentEntity a WHERE a.task.column.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT a.filePath FROM AttachmentEntity a WHERE a.task.column.project.id = :projectId")
    List<String> findFilePathsByProjectId(@Param("projectId") UUID projectId);

    @Query("""
    SELECT new com.stodo.projectchaos.features.attachment.AttachmentInfo(
        a.id, a.project.id, a.task.id, a.user.id,
        a.fileName, a.originalName, a.filePath,
        a.contentType, a.fileSizeInBytes, a.vectorStatus, a.storageStatus
    )
    FROM AttachmentEntity a
    WHERE a.project.id = :projectId AND a.task.id = :taskId
    """)
    List<AttachmentInfo> findByProjectIdAndTaskId(@Param("projectId") UUID projectId, @Param("taskId") UUID taskId);

    @Query("SELECT a.filePath FROM AttachmentEntity a WHERE a.project.id = :projectId AND a.task.id = :taskId AND a.id = :attachmentId")
    Optional<String> getFilePathByProjectIdAndTaskIdAndId(@Param("projectId") UUID projectId, @Param("taskId") UUID taskId, @Param("attachmentId") UUID attachmentId);
}