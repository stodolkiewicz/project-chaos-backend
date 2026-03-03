package com.stodo.projectchaos.features.label.dto.mapper;

import com.stodo.projectchaos.features.label.dto.response.LabelDTO;
import com.stodo.projectchaos.model.entity.LabelEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LabelMapper {
    LabelMapper INSTANCE = Mappers.getMapper(LabelMapper.class);

    List<LabelDTO> toLabelDTOList(List<LabelEntity> labels);

}