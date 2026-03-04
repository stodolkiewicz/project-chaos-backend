package com.stodo.projectchaos.features.priority.dto.mapper;

import com.stodo.projectchaos.model.entity.TaskPriorityEntity;
import com.stodo.projectchaos.features.priority.dto.service.TaskPriority;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskPriorityEntityMapper {
    TaskPriorityEntityMapper INSTANCE = Mappers.getMapper(TaskPriorityEntityMapper.class);

    TaskPriority toTaskPriority(TaskPriorityEntity entity);
}