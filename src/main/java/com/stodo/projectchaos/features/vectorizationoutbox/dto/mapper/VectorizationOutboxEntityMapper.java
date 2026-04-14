package com.stodo.projectchaos.features.vectorizationoutbox.dto.mapper;

import com.stodo.projectchaos.features.vectorizationoutbox.dto.service.VectorizationOutbox;
import com.stodo.projectchaos.model.entity.VectorizationOutboxEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VectorizationOutboxEntityMapper {

    VectorizationOutboxEntityMapper INSTANCE = Mappers.getMapper(VectorizationOutboxEntityMapper.class);

    @Mapping(source = "attachment.id", target = "attachmentId")
    VectorizationOutbox toVectorizationOutbox(VectorizationOutboxEntity entity);
}
