package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.TaskLabelsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskLabelsRepository extends JpaRepository<TaskLabelsEntity, UUID> {

    @Modifying
    @Query("DELETE FROM TaskLabelsEntity tl WHERE tl.task.column.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
