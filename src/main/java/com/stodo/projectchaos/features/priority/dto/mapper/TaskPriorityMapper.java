package com.stodo.projectchaos.features.priority.dto.mapper;

import com.stodo.projectchaos.model.entity.TaskPriorityEntity;
import com.stodo.projectchaos.features.priority.dto.response.TaskPriorityResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TaskPriorityMapper {
    TaskPriorityMapper INSTANCE = Mappers.getMapper(TaskPriorityMapper.class);

    List<TaskPriorityResponseDTO> toTaskPriorityResponseDTOList(List<TaskPriorityEntity> entities);
}