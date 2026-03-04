package com.stodo.projectchaos.features.priority.dto.mapper;

import com.stodo.projectchaos.features.priority.dto.service.TaskPriority;
import com.stodo.projectchaos.features.priority.dto.response.TaskPriorityResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TaskPriorityMapper {
    TaskPriorityMapper INSTANCE = Mappers.getMapper(TaskPriorityMapper.class);

    TaskPriorityResponseDTO toTaskPriorityResponseDTO(TaskPriority taskPriority);
    
    default List<TaskPriorityResponseDTO> toTaskPriorityResponseDTOList(List<TaskPriority> taskPriorities) {
        return taskPriorities.stream()
                .map(this::toTaskPriorityResponseDTO)
                .toList();
    }
}