package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.task.create.request.CreateTaskRequestDTO;
import com.stodo.projectchaos.model.dto.task.create.request.LabelDTO;
import com.stodo.projectchaos.model.dto.task.create.response.CreateTaskResponseDTO;
import com.stodo.projectchaos.model.entity.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class CustomTaskRepository {

    private final UserRepository userRepository;
    private final ColumnRepository columnRepository;
    private final ProjectBacklogRepository projectBacklogRepository;
    private final TaskPriorityRepository taskPriorityRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final LabelRepository labelRepository;
    private final TaskLabelsRepository taskLabelsRepository;

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

        ProjectEntity projectEntity = projectRepository.findById(projectId).orElseThrow(() ->
                EntityNotFoundException.builder()
                        .entityType("Project")
                        .identifier("id", projectId.toString())
                        .build()
        );

        // todo - check this when project_backlog is worked on
        // if columnId is null, create a project_backlog
        if(requestDTO.columnId() == null) {
            ProjectBacklogEntity projectBacklogEntity = new ProjectBacklogEntity();
            projectBacklogEntity.setProject(projectEntity);
            projectBacklogEntity.setTask(savedTaskEntity);

            projectBacklogRepository.save(projectBacklogEntity);
        }

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

        return CreateTaskResponseDTO.fromEntity(savedTaskEntity);
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


