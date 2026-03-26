package com.stodo.projectchaos.storage.projectlimit.dto.mapper;

import com.stodo.projectchaos.model.entity.ProjectStorageUsageEntity;
import com.stodo.projectchaos.storage.projectlimit.dto.service.ProjectStorageUsage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectStorageUsageEntityMapper {
    ProjectStorageUsageEntityMapper INSTANCE = Mappers.getMapper(ProjectStorageUsageEntityMapper.class);

    @Mapping(source = "project.id", target = "projectId")
    ProjectStorageUsage toProjectStorageUsage(ProjectStorageUsageEntity entity);
}