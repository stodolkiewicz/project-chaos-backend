package com.stodo.projectchaos.embedding.dto.mapper;

import com.stodo.projectchaos.embedding.dto.service.AttachmentEmbedding;
import com.stodo.projectchaos.model.entity.AttachmentEmbeddingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AttachmentEmbeddingEntityMapper {
    AttachmentEmbeddingEntityMapper INSTANCE = Mappers.getMapper(AttachmentEmbeddingEntityMapper.class);

    @Mapping(source = "attachment.id", target = "attachmentId")
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "content", target = "content")
    AttachmentEmbedding toAttachmentEmbedding(AttachmentEmbeddingEntity entity);
}