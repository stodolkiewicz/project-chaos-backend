package com.stodo.projectchaos.features.taskcomments.dto.response;

import java.util.List;
import java.util.UUID;

public record TaskCommentsResponseDTO(
        UUID projectId,
        UUID taskId,
        List<TaskCommentResponseDTO> taskComments
) {
}
