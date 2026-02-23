package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, UUID> {

    @Modifying
    @Query("DELETE FROM AttachmentEntity a WHERE a.task.column.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
    
    @Query("SELECT a.fileUrl FROM AttachmentEntity a WHERE a.task.column.project.id = :projectId")
    List<String> findFilePathsByProjectId(@Param("projectId") UUID projectId);
}