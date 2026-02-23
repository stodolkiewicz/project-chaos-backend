package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.ProjectUserId;
import com.stodo.projectchaos.model.entity.ProjectUsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectUsersRepository extends JpaRepository<ProjectUsersEntity, ProjectUserId> {
    
    @Modifying
    @Query("DELETE FROM ProjectUsersEntity pu WHERE pu.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
