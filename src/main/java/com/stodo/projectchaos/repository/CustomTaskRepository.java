package com.stodo.projectchaos.repository;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.request.CreateTaskRequestDTO;
import com.stodo.projectchaos.model.dto.response.createtask.CreateTaskResponseDTO;
import com.stodo.projectchaos.model.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CustomTaskRepository {

    UserRepository userRepository;
    ColumnRepository columnRepository;
    ProjectBacklogRepository projectBacklogRepository;
    TaskPriorityRepository taskPriorityRepository;
    TaskRepository taskRepository;
    ProjectRepository projectRepository;
    LabelRepository labelRepository;
    TaskLabelsRepository taskLabelsRepository;

    public CustomTaskRepository(
            UserRepository userRepository,
            ColumnRepository columnRepository,
            ProjectBacklogRepository projectBacklogRepository,
            TaskPriorityRepository taskPriorityRepository,
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            LabelRepository labelRepository,
            TaskLabelsRepository taskLabelsRepository
            ) {
        this.userRepository = userRepository;
        this.columnRepository = columnRepository;
        this.projectBacklogRepository = projectBacklogRepository;
        this.taskPriorityRepository = taskPriorityRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.labelRepository = labelRepository;
        this.taskLabelsRepository = taskLabelsRepository;
    }

    @Transactional
    public CreateTaskResponseDTO createTask(CreateTaskRequestDTO requestDTO, UUID projectId) {
        String email = requestDTO.assigneeEmail();

        UserEntity userEntity = userRepository.findByEmail(email).orElse(null);

        TaskPriorityEntity taskPriorityEntity = requestDTO.priorityId() == null ? null : taskPriorityRepository.findById(requestDTO.priorityId()).orElseThrow(() ->
                EntityNotFoundException.builder()
                        .entityType("TaskPriority")
                        .identifier("id", requestDTO.priorityId().toString())
                        .build()
        );

        ColumnEntity column = null;
        if(requestDTO.columnId() != null) {
            column = columnRepository.findById(requestDTO.columnId()).orElse(null);
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
                .build();

        TaskEntity savedTaskEntity = taskRepository.save(taskEntityToBeSaved);

        // if columnId is null, create a project_backlog
        if(requestDTO.columnId() == null) {
            ProjectEntity projectEntity = projectRepository.findById(projectId).orElseThrow(() ->
                    EntityNotFoundException.builder()
                            .entityType("Project")
                            .identifier("id", projectId.toString())
                            .build()
            );

            ProjectBacklogEntity projectBacklogEntity = new ProjectBacklogEntity();
            projectBacklogEntity.setProject(projectEntity);
            projectBacklogEntity.setTask(savedTaskEntity);

            projectBacklogRepository.save(projectBacklogEntity);
        }

        // save the labels
        if (requestDTO.labels() != null && !requestDTO.labels().isEmpty()) {
            Set<TaskLabelsEntity> taskLabels = new HashSet<>();
            for (String rawLabelName : requestDTO.labels()) {
                String normalizedLabelName = rawLabelName.trim().toLowerCase();

                LabelEntity label = labelRepository.findByName(normalizedLabelName)
                        .orElseGet(() -> {
                            LabelEntity newLabel = LabelEntity.builder()
                                    .id(UUID.randomUUID())
                                    .name(normalizedLabelName)
                                    .build();
                            return labelRepository.save(newLabel);
                        });

                TaskLabelsEntity taskLabel = TaskLabelsEntity.builder()
                        .task(savedTaskEntity)
                        .label(label)
                        .id(new TaskLabelId(savedTaskEntity.getId(), label.getId()))
                        .build();

                taskLabels.add(taskLabel);
            }
            taskLabelsRepository.saveAll(taskLabels);
            savedTaskEntity.setTaskLabels(taskLabels);
        }

        return CreateTaskResponseDTO.fromEntity(savedTaskEntity);
    }
}
