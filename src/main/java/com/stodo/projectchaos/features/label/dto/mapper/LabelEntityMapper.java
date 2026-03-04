package com.stodo.projectchaos.features.label.dto.mapper;

import com.stodo.projectchaos.model.entity.LabelEntity;
import com.stodo.projectchaos.features.label.dto.service.Labels;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

@Mapper
public interface LabelEntityMapper {
    LabelEntityMapper INSTANCE = Mappers.getMapper(LabelEntityMapper.class);

    Labels.Label toLabel(LabelEntity entity);
    
    default Labels toLabels(List<LabelEntity> entities, UUID projectId) {
        List<Labels.Label> labels = entities.stream()
                .map(this::toLabel)
                .toList();
        return new Labels(projectId, labels);
    }
}