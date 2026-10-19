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

    // Returns [taskId, count] rows; tasks without unread comments are absent
    @Query("""
            SELECT tc.task.id, COUNT(tc)
            FROM TaskComments tc
            LEFT JOIN TaskCommentReadEntity r ON r.task.id = tc.task.id AND r.user.id = :userId
            WHERE tc.task.id IN :taskIds
            AND tc.author.id <> :userId
            AND (r.lastReadAt IS NULL OR tc.createdDate > r.lastReadAt)
            GROUP BY tc.task.id
            """)
    List<Object[]> countUnreadCommentsByTaskIds(@Param("taskIds") List<UUID> taskIds, @Param("userId") UUID userId);

}