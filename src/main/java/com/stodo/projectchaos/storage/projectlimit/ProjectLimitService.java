package com.stodo.projectchaos.storage.projectlimit;

import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.ProjectStorageUsageEntity;
import com.stodo.projectchaos.features.project.ProjectRepository;
import com.stodo.projectchaos.storage.projectlimit.dto.service.ProjectStorageUsage;
import com.stodo.projectchaos.storage.projectlimit.dto.mapper.ProjectStorageUsageEntityMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectLimitService {
    
    private final ProjectStorageUsageRepository projectStorageUsageRepository;
    private final ProjectRepository projectRepository;
    
    @Value("${storage.limits.project.default}")
    private long defaultProjectLimit;

    public boolean canUpload(UUID projectId, long bytesToUpload) {
        ProjectStorageUsageEntity usage = projectStorageUsageRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalStateException("Project storage usage not found for project: " + projectId));
        
        return (usage.getUsedBytes() + bytesToUpload) <= usage.getLimitBytes();
    }

    public void increaseUsedBytes(UUID projectId, long bytes) {
        projectStorageUsageRepository.increaseUsedBytes(projectId, bytes);
    }

    public void createProjectStorageUsage(UUID projectId) {
        ProjectEntity project = projectRepository.getReferenceById(projectId);
        
        ProjectStorageUsageEntity usage = ProjectStorageUsageEntity.builder()
                .id(UUID.randomUUID())
                .project(project)
                .usedBytes(0L)
                .limitBytes(defaultProjectLimit)
                .build();
        
        projectStorageUsageRepository.save(usage);
    }

    public ProjectStorageUsage getProjectStorageUsage(UUID projectId) {
        ProjectStorageUsageEntity entity = projectStorageUsageRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalStateException("Project storage usage not found for project: " + projectId));
        
        return ProjectStorageUsageEntityMapper.INSTANCE.toProjectStorageUsage(entity);
    }
}