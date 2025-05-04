package com.stodo.projectchaos.model.dto.task.columnupdate.response;

import com.stodo.projectchaos.model.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UpdateTaskColumnMapper {

    UpdateTaskColumnMapper INSTANCE = Mappers.getMapper(UpdateTaskColumnMapper.class);

    UpdateTaskColumnResponseDTO toUpdateTaskColumnResponseDTO(TaskEntity task);
}
