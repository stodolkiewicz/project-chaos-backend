package com.stodo.projectchaos.model.dto.task.board.response;

import com.stodo.projectchaos.model.entity.LabelEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LabelMapper {
    LabelMapper INSTANCE = Mappers.getMapper(LabelMapper.class);

    @Mapping(source = "color", target = "color")
    LabelDTO toLabelDTO(LabelEntity label);
}
