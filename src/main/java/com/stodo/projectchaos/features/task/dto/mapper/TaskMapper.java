package com.stodo.projectchaos.features.task.dto.mapper;

import com.stodo.projectchaos.features.task.dto.response.CreateTaskResponseDTO;
import com.stodo.projectchaos.features.task.dto.response.TasksResponseDTO;
import com.stodo.projectchaos.features.task.dto.service.Task;
import com.stodo.projectchaos.features.task.dto.service.BoardTask;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskMapper {
    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

    CreateTaskResponseDTO toCreateTaskResponseDTO(Task task);
    
    TasksResponseDTO toBoardTasksResponseDTO(BoardTask boardTask);
}