package com.stodo.projectchaos.features.label.dto.mapper;

import com.stodo.projectchaos.features.label.dto.response.LabelDTO;
import com.stodo.projectchaos.features.label.dto.response.LabelResponseDTO;
import com.stodo.projectchaos.features.label.dto.service.Labels;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LabelMapper {
    LabelMapper INSTANCE = Mappers.getMapper(LabelMapper.class);

    LabelResponseDTO toLabelResponseDTO(Labels labels);
    
    LabelDTO toLabelDTO(Labels.Label label);
    
    default List<LabelDTO> toLabelDTOList(List<Labels.Label> labels) {
        return labels.stream()
                .map(this::toLabelDTO)
                .toList();
    }

}