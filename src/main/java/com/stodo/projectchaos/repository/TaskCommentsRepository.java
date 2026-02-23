package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.TaskComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskCommentsRepository extends JpaRepository<TaskComments, UUID> {

    @Modifying
    @Query("DELETE FROM TaskComments tc WHERE tc.task.column.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}