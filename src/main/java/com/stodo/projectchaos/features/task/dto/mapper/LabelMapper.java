package com.stodo.projectchaos.features.task.dto.mapper;

import com.stodo.projectchaos.model.entity.LabelEntity;
import com.stodo.projectchaos.features.task.dto.request.LabelDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LabelMapper {
    LabelMapper INSTANCE = Mappers.getMapper(LabelMapper.class);

    @Mapping(source = "color", target = "color")
    LabelDTO toLabelDTO(LabelEntity label);
}
