package com.stodo.projectchaos.mapper;

import com.stodo.projectchaos.model.dto.response.taskpriorities.TaskPriorityResponseDTO;
import com.stodo.projectchaos.model.entity.TaskPriorityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TaskPriorityMapper {
    TaskPriorityMapper INSTANCE = Mappers.getMapper(TaskPriorityMapper.class);

    List<TaskPriorityResponseDTO> toTaskPriorityResponseDTOList(List<TaskPriorityEntity> entities);
}