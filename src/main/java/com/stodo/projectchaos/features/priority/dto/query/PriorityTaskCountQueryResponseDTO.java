package com.stodo.projectchaos.features.priority.dto.query;

import java.util.UUID;

public record PriorityTaskCountQueryResponseDTO(UUID priorityId, long taskCount) {
}
