package com.stodo.projectchaos.storage.projectlimit;

import com.stodo.projectchaos.model.entity.ProjectStorageUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectStorageUsageRepository extends JpaRepository<ProjectStorageUsageEntity, UUID> {

    Optional<ProjectStorageUsageEntity> findByProjectId(UUID projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ProjectStorageUsageEntity p SET p.usedBytes = p.usedBytes + :bytes WHERE p.project.id = :projectId")
    void increaseUsedBytes(@Param("projectId") UUID projectId, @Param("bytes") long bytes);
}