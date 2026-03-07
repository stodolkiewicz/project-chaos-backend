package com.stodo.projectchaos.features.taskcomments.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TaskCommentResponseDTO(
        UUID id,
        String content,
        String lastModifiedBy,
        Instant lastModifiedDate,
        UUID authorId,
        UUID replyToId
){
}
