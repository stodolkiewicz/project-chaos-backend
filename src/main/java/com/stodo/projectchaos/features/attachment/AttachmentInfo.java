package com.stodo.projectchaos.features.attachment;

import com.stodo.projectchaos.model.enums.StorageStatusEnum;
import com.stodo.projectchaos.model.enums.VectorStatusEnum;

import java.util.UUID;

public record AttachmentInfo(
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
) {
    public AttachmentInfo(UUID id, UUID projectId, UUID taskId, UUID userId, String fileName, String originalName, String filePath, String contentType, Long fileSizeInBytes, VectorStatusEnum vectorStatus, StorageStatusEnum storageStatus) {
        this(id, projectId, taskId, userId, fileName, originalName, filePath, contentType, fileSizeInBytes, vectorStatus, storageStatus, null);
    }

    public AttachmentInfo withUrl(String url) {
        return new AttachmentInfo(id, projectId, taskId, userId, fileName, originalName, filePath, contentType, fileSizeInBytes, vectorStatus, storageStatus, url);
    }
}