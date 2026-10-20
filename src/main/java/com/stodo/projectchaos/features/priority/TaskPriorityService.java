package com.stodo.projectchaos.features.priority;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.priority.dto.query.PriorityTaskCountQueryResponseDTO;
import com.stodo.projectchaos.features.priority.dto.service.TaskPriority;
import com.stodo.projectchaos.features.priority.dto.mapper.TaskPriorityEntityMapper;
import com.stodo.projectchaos.features.task.TaskRepository;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.TaskPriorityEntity;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskPriorityService {
    private final TaskPriorityRepository taskPriorityRepository;
    private final TaskRepository taskRepository;
    private final EntityManager entityManager;

    public List<TaskPriority> findTaskPrioritiesByProjectId(UUID projectId) {
        Map<UUID, Long> taskCounts = taskPriorityRepository.countTasksByPriority(projectId)
                .stream()
                .collect(Collectors.toMap(
                        PriorityTaskCountQueryResponseDTO::priorityId,
                        PriorityTaskCountQueryResponseDTO::taskCount
                ));

        return taskPriorityRepository.findByProjectIdOrderByPriorityValue(projectId)
                .stream()
                .map(entity -> TaskPriorityEntityMapper.INSTANCE.toTaskPriority(
                        entity,
                        taskCounts.getOrDefault(entity.getId(), 0L)
                ))
                .toList();
    }

    // Existing priorities missing from the list are deleted, and their tasks lose the priority.
    // A priority keeps its id when its name, value or color changes, so its tasks keep it too.
    @Transactional
    public void updateTaskPriorities(UUID projectId, UUID userId, List<TaskPriority> priorities) {
        Set<Short> usedValues = new HashSet<>();
        for (TaskPriority priority : priorities) {
            if (!usedValues.add(priority.priorityValue())) {
                throw new DuplicatePriorityValueException(priority.priorityValue());
            }
        }

        Set<UUID> existingIds = taskPriorityRepository.findByProjectIdOrderByPriorityValue(projectId)
                .stream()
                .map(TaskPriorityEntity::getId)
                .collect(Collectors.toSet());
        Set<UUID> keptIds = priorities.stream()
                .map(TaskPriority::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (UUID priorityId : keptIds) {
            if (!existingIds.contains(priorityId)) {
                throw EntityNotFoundException.builder()
                        .identifier("priorityId", priorityId)
                        .identifier("projectId", projectId)
                        .entityType("TaskPriorityEntity")
                        .build();
            }
        }

        Set<UUID> removedIds = new HashSet<>(existingIds);
        removedIds.removeAll(keptIds);
        if (!removedIds.isEmpty()) {
            taskRepository.clearPriorityByPriorityIds(removedIds);
            taskPriorityRepository.deleteAllByIdInBatch(removedIds);
        }

        // Clearing the tasks detaches the loaded entities, so the kept priorities are read again
        Map<UUID, TaskPriorityEntity> keptEntities = taskPriorityRepository.findAllById(keptIds)
                .stream()
                .collect(Collectors.toMap(TaskPriorityEntity::getId, Function.identity()));

        List<TaskPriorityEntity> entities = priorities.stream()
                .map(priority -> {
                    TaskPriorityEntity entity;
                    if (priority.id() == null) {
                        entity = new TaskPriorityEntity();
                        entity.setProject(entityManager.getReference(ProjectEntity.class, projectId));
                    } else {
                        entity = keptEntities.get(priority.id());
                    }
                    entity.setName(priority.name().trim());
                    entity.setPriorityValue(priority.priorityValue());
                    entity.setColor(priority.color());
                    return entity;
                })
                .toList();
        taskPriorityRepository.saveAll(entities);

        log.info("Task priorities updated [projectId={}, userId={}]",
                projectId,
                userId);
    }
}
