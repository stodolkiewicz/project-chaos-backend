package com.stodo.projectchaos.features.task;

import com.stodo.projectchaos.model.entity.TaskLabelsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskLabelsRepository extends JpaRepository<TaskLabelsEntity, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TaskLabelsEntity tl WHERE tl.task.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
