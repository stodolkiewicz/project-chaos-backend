package com.stodo.projectchaos.features.task;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.task.dto.request.CreateTaskRequestDTO;
import com.stodo.projectchaos.features.task.dto.request.LabelDTO;
import com.stodo.projectchaos.features.task.dto.request.UpdateTaskRequestDTO;
import com.stodo.projectchaos.features.task.dto.service.Task;
import com.stodo.projectchaos.features.task.dto.mapper.TaskEntityMapper;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.model.entity.TaskPriorityEntity;
import com.stodo.projectchaos.features.priority.TaskPriorityRepository;
import com.stodo.projectchaos.model.entity.ColumnEntity;
import com.stodo.projectchaos.features.column.ColumnRepository;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.features.project.ProjectRepository;
import com.stodo.projectchaos.model.entity.LabelEntity;
import com.stodo.projectchaos.features.label.LabelRepository;
import com.stodo.projectchaos.model.entity.TaskEntity;
import com.stodo.projectchaos.model.entity.TaskLabelsEntity;
import com.stodo.projectchaos.model.entity.TaskLabelId;
import com.stodo.projectchaos.model.enums.TaskStageEnum;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomTaskRepository {

    private final UserRepository userRepository;
    private final ColumnRepository columnRepository;
    private final TaskPriorityRepository taskPriorityRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final LabelRepository labelRepository;
    private final TaskLabelsRepository taskLabelsRepository;
    private final MeterRegistry registry;
    private Counter tasksCreatedCounter;

    @PostConstruct
    void initMetrics() {
        tasksCreatedCounter = Counter.builder("projectchaos.tasks.created")
                .description("Total number of tasks created")
                .register(registry);
    }

    @Transactional
    public Task createTask(CreateTaskRequestDTO requestDTO, UUID projectId) {
        String email = requestDTO.assigneeEmail();

        UserEntity userEntity = userRepository.findByEmail(email).orElse(null);

        TaskPriorityEntity taskPriorityEntity = requestDTO.priorityId() == null ? null : taskPriorityRepository.findById(requestDTO.priorityId()).orElseThrow(() ->
                EntityNotFoundException.builder()
                        .entityType("TaskPriority")
                        .identifier("id", requestDTO.priorityId().toString())
                        .build()
        );

        ProjectEntity projectEntity = projectRepository.findById(projectId).orElseThrow(() ->
                EntityNotFoundException.builder()
                        .entityType("Project")
                        .identifier("id", projectId.toString())
                        .build()
        );

        ColumnEntity column = null;
        if(requestDTO.columnId() != null) {
            column = columnRepository.findById(requestDTO.columnId()).orElse(null);
        }

        // Validate stage and column relationship
        if (requestDTO.stage() == TaskStageEnum.BOARD && requestDTO.columnId() == null) {
            throw new IllegalArgumentException("Column ID is required when stage is BOARD");
        }

        // create a task entity
        TaskEntity taskEntityToBeSaved = TaskEntity.builder()
                .id(UUID.randomUUID())
                .title(requestDTO.title())
                .description(requestDTO.description())
                .positionInColumn(requestDTO.positionInColumn())
                .assignee(userEntity)
                .column(column)
                .priority(taskPriorityEntity)
                .project(projectEntity)
                .stage(requestDTO.stage())
                .build();

        TaskEntity savedTaskEntity = taskRepository.save(taskEntityToBeSaved);

        // save the labels
        if (requestDTO.labels() != null && !requestDTO.labels().isEmpty()) {
            Set<TaskLabelsEntity> taskLabels = new HashSet<>();

            for (LabelDTO labelDTO : requestDTO.labels()) {
                String normalizedLabelName = labelDTO.name().trim().toLowerCase();
                String color = labelDTO.color();

                LabelEntity labelEntity = saveOrUpdateLabel(normalizedLabelName, color, projectEntity);

                TaskLabelsEntity taskLabel = TaskLabelsEntity.builder()
                        .task(savedTaskEntity)
                        .label(labelEntity)
                        .id(new TaskLabelId(savedTaskEntity.getId(), labelEntity.getId()))
                        .build();

                taskLabels.add(taskLabel);
            }
            taskLabelsRepository.saveAll(taskLabels);
            savedTaskEntity.setTaskLabels(taskLabels);
        }

        log.info("Task created [taskId={}, projectId={}, projectName={}]",
                savedTaskEntity.getId(),
                projectId,
                projectEntity.getName());

        // Increments the counter only after the transaction is committed. A rollback does not count the task.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        tasksCreatedCounter.increment();
                    }
                });

        return TaskEntityMapper.INSTANCE.toTask(savedTaskEntity);
    }

    @Transactional
    public void updateTask(UUID projectId, UUID taskId, UpdateTaskRequestDTO requestDTO) {
        TaskEntity taskEntity = taskRepository.findAllByIdInAndProjectId(List.of(taskId), projectId)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        EntityNotFoundException.builder()
                                .entityType("Task")
                                .identifier("id", taskId.toString())
                                .build()
                );

        String email = requestDTO.assigneeEmail();
        UserEntity assignee = email == null || email.isBlank() ? null : userRepository.findByEmail(email).orElseThrow(() ->
                EntityNotFoundException.builder()
                        .entityType("User")
                        .identifier("email", email)
                        .build()
        );

        TaskPriorityEntity priority = requestDTO.priorityId() == null ? null : taskPriorityRepository.findById(requestDTO.priorityId()).orElseThrow(() ->
                EntityNotFoundException.builder()
                        .entityType("TaskPriority")
                        .identifier("id", requestDTO.priorityId().toString())
                        .build()
        );

        taskEntity.setTitle(requestDTO.title());
        taskEntity.setDescription(requestDTO.description());
        taskEntity.setAssignee(assignee);
        taskEntity.setPriority(priority);

        // Keyed by label id, so duplicated names in the request end up as one label
        Map<UUID, LabelEntity> requestedLabels = new HashMap<>();
        if (requestDTO.labels() != null) {
            for (LabelDTO labelDTO : requestDTO.labels()) {
                String normalizedLabelName = labelDTO.name().trim().toLowerCase();
                LabelEntity labelEntity = saveOrUpdateLabel(normalizedLabelName, labelDTO.color(), taskEntity.getProject());
                requestedLabels.put(labelEntity.getId(), labelEntity);
            }
        }

        // Removed links are deleted by orphanRemoval
        Set<TaskLabelsEntity> taskLabels = taskEntity.getTaskLabels();
        taskLabels.removeIf(taskLabel -> !requestedLabels.containsKey(taskLabel.getLabel().getId()));

        Set<UUID> currentLabelIds = new HashSet<>();
        taskLabels.forEach(taskLabel -> currentLabelIds.add(taskLabel.getLabel().getId()));

        requestedLabels.values().stream()
                .filter(labelEntity -> !currentLabelIds.contains(labelEntity.getId()))
                .forEach(labelEntity -> taskLabels.add(
                        TaskLabelsEntity.builder()
                                .task(taskEntity)
                                .label(labelEntity)
                                .id(new TaskLabelId(taskEntity.getId(), labelEntity.getId()))
                                .build()
                ));

        // Flushes the label changes first, so labels removed from their last task are deleted
        labelRepository.deleteUnusedLabels(projectId);

        log.info("Task updated [taskId={}, projectId={}]",
                taskId,
                projectId);
    }

    private LabelEntity saveOrUpdateLabel(String normalizedLabelName, String color, ProjectEntity projectEntity) {
        Optional<LabelEntity> existingLabelOpt = labelRepository.findByNameAndProjectId(normalizedLabelName, projectEntity.getId());

        if (existingLabelOpt.isPresent()) {
            LabelEntity existingLabel = existingLabelOpt.get();

            // label found, but we have to override the color
            if (!existingLabel.getColor().equals(color)) {
                existingLabel.setColor(color);
                return labelRepository.save(existingLabel);
            }

            // label found, color is the same, no need to update
            return existingLabel;
        } else {
            // label not found, create a new one
            LabelEntity newLabel = LabelEntity.builder()
                    .id(UUID.randomUUID())
                    .name(normalizedLabelName)
                    .color(color)
                    .project(projectEntity)
                    .build();
            return labelRepository.save(newLabel);
        }
    }
}


