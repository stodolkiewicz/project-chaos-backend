package com.stodo.projectchaos.features.attachment.dto.mapper;

import com.stodo.projectchaos.features.attachment.AttachmentInfo;
import com.stodo.projectchaos.features.attachment.dto.response.AttachmentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AttachmentMapper {
    AttachmentMapper INSTANCE = Mappers.getMapper(AttachmentMapper.class);

    AttachmentResponseDTO toAttachmentResponseDTO(AttachmentInfo attachment);
}