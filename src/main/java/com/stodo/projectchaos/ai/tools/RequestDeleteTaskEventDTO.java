package com.stodo.projectchaos.ai.tools;

import java.util.UUID;

public record RequestDeleteTaskEventDTO(UUID taskId, String title) {
}
