package com.stodo.projectchaos.features.taskcomments.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TaskCommentWithRepliesResponseDTO(
        UUID id,
        String content,
        String lastModifiedBy,
        Instant lastModifiedDate,
        UUID authorId,
        UUID replyToId,
        List<TaskCommentWithRepliesResponseDTO> replies
) {
}