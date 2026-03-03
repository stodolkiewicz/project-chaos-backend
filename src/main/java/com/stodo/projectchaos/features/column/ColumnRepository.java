package com.stodo.projectchaos.features.column;

import com.stodo.projectchaos.model.entity.ColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ColumnRepository extends JpaRepository<ColumnEntity, UUID> {
    List<ColumnEntity> findByProjectIdOrderByPosition(UUID projectId);
    
    @Modifying
    @Query("DELETE FROM ColumnEntity c WHERE c.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
