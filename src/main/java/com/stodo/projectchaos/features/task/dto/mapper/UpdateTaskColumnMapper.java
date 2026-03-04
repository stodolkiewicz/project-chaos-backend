package com.stodo.projectchaos.features.task.dto.mapper;

import com.stodo.projectchaos.features.task.dto.response.UpdateTaskColumnResponseDTO;
import com.stodo.projectchaos.features.task.dto.service.TaskColumnUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UpdateTaskColumnMapper {

    UpdateTaskColumnMapper INSTANCE = Mappers.getMapper(UpdateTaskColumnMapper.class);

    UpdateTaskColumnResponseDTO toUpdateTaskColumnResponseDTO(TaskColumnUpdate taskColumnUpdate);
}
