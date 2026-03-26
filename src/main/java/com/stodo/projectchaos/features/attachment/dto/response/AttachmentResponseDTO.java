package com.stodo.projectchaos.features.attachment.dto.response;

import com.stodo.projectchaos.model.enums.StorageStatusEnum;
import com.stodo.projectchaos.model.enums.VectorStatusEnum;

import java.util.UUID;

public record AttachmentResponseDTO(
        UUID id,
        UUID projectId,
        UUID taskId,
        UUID userId,
        String fileName,
        String originalName,
        String filePath,
        String contentType,
        Long fileSizeInBytes,
        VectorStatusEnum vectorStatus,
        StorageStatusEnum storageStatus,
        String presignedUrl
) {}