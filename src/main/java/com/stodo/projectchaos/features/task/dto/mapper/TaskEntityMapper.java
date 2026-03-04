package com.stodo.projectchaos.features.task.dto.mapper;

import com.stodo.projectchaos.model.entity.TaskEntity;
import com.stodo.projectchaos.features.task.dto.service.Task;
import com.stodo.projectchaos.features.task.dto.service.TaskColumnUpdate;
import com.stodo.projectchaos.features.task.dto.service.BoardTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskEntityMapper {
    TaskEntityMapper INSTANCE = Mappers.getMapper(TaskEntityMapper.class);

    @Mapping(target = "columnId", source = "column.id")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "priorityId", source = "priority.id")
    Task toTask(TaskEntity entity);
    
    @Mapping(target = "taskId", source = "id")
    @Mapping(target = "columnId", source = "column.id")
    TaskColumnUpdate toTaskColumnUpdate(TaskEntity entity);
    
    BoardTask toBoardTask(TaskEntity entity);
}