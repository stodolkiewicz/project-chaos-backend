package com.stodo.projectchaos.embedding.dto.service;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AttachmentEmbedding {
    UUID id;
    UUID attachmentId;
    UUID taskId;
    UUID projectId;
    Integer chunkIndex;
    String content;
    float[] embedding;
    Instant createdDate;
}