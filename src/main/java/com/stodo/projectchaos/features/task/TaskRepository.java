package com.stodo.projectchaos.features.task;

import com.stodo.projectchaos.model.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TaskEntity t WHERE t.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);

    @Query("""
        SELECT CASE WHEN COUNT(t) = 0 THEN true ELSE false END
        FROM TaskEntity t
        WHERE t.id = :taskId AND t.project.id = :projectId
    """)
    boolean notExistByIdAndProjectId(UUID taskId, UUID projectId);

    @Query("SELECT MAX(t.positionInColumn) FROM TaskEntity t WHERE t.column.id = :columnId")
    Double findMaxPositionInColumnByColumnId(@Param("columnId") UUID columnId);

    List<TaskEntity> findAllByIdInAndProjectId(List<UUID> taskIds, UUID projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TaskEntity t SET t.priority = NULL WHERE t.project.id = :projectId")
    void clearPriorityByProjectId(@Param("projectId") UUID projectId);
}
