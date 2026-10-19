package com.stodo.projectchaos.features.task.dto.request;

import java.util.List;
import java.util.UUID;

// Values inside one filter are alternatives; different filters must all match.
// An empty filter (no ids and no "without" flag) matches every task.
public record TasksFilterRequestDTO(
        List<UUID> priorityIds,
        Boolean withoutPriority,
        List<UUID> labelIds,
        Boolean withoutLabel,
        List<UUID> assigneeIds,
        Boolean unassigned
) {
    public TasksFilterRequestDTO {
        priorityIds = priorityIds == null ? List.of() : priorityIds;
        withoutPriority = Boolean.TRUE.equals(withoutPriority);
        labelIds = labelIds == null ? List.of() : labelIds;
        withoutLabel = Boolean.TRUE.equals(withoutLabel);
        assigneeIds = assigneeIds == null ? List.of() : assigneeIds;
        unassigned = Boolean.TRUE.equals(unassigned);
    }

    public static TasksFilterRequestDTO none() {
        return new TasksFilterRequestDTO(null, null, null, null, null, null);
    }
}
