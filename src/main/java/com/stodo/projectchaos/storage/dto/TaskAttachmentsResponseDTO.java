package com.stodo.projectchaos.storage.dto;

import com.stodo.projectchaos.features.attachment.dto.response.AttachmentResponseDTO;

import java.util.List;

public record TaskAttachmentsResponseDTO(
        List<AttachmentResponseDTO> attachments
) {}