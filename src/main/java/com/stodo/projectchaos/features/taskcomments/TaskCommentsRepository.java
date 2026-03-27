package com.stodo.projectchaos.features.taskcomments;

import com.stodo.projectchaos.model.entity.TaskComments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCommentsRepository extends JpaRepository<TaskComments, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TaskComments tc WHERE tc.task.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);

    List<TaskComments> findByTaskId(UUID taskId);

    @Query("SELECT tc.id FROM TaskComments tc WHERE tc.task.id = :taskId AND tc.replyTo IS NULL ORDER BY tc.createdDate ASC")
    Page<UUID> findRootCommentIds(@Param("taskId") UUID taskId, Pageable pageable);

}