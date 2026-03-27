package com.stodo.projectchaos.features.priority;

import com.stodo.projectchaos.model.entity.TaskPriorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskPriorityRepository extends JpaRepository<TaskPriorityEntity, UUID> {
    List<TaskPriorityEntity> findByProjectId(UUID projectId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TaskPriorityEntity tp WHERE tp.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
