package com.stodo.projectchaos.service;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.task.columnupdate.response.UpdateTaskColumnMapper;
import com.stodo.projectchaos.model.dto.task.create.request.CreateTaskRequestDTO;
import com.stodo.projectchaos.model.dto.task.board.response.BoardTasksResponseDTO;
import com.stodo.projectchaos.model.dto.task.create.response.CreateTaskResponseDTO;
import com.stodo.projectchaos.model.dto.task.columnupdate.request.UpdateTaskColumnRequestDTO;
import com.stodo.projectchaos.model.dto.task.columnupdate.response.UpdateTaskColumnResponseDTO;
import com.stodo.projectchaos.model.entity.ColumnEntity;
import com.stodo.projectchaos.model.entity.TaskEntity;
import com.stodo.projectchaos.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {
    private final CustomBoardRepository customBoardRepository;
    private final CustomTaskRepository customTaskRepository;
    private final TaskRepository taskRepository;
    private final ColumnRepository columnRepository;
    private final LabelRepository labelRepository;

    private static final Double MINIMUM_DISTANCE_BETWEEN_TASKS = 0.000001;

    public List<BoardTasksResponseDTO> getBoardTasks(UUID projectId) {
        return customBoardRepository.findBoardTasks(projectId);
    }

    public CreateTaskResponseDTO createTask(CreateTaskRequestDTO requestDTO, UUID projectId) {
        return customTaskRepository.createTask(requestDTO, projectId);
    }

    public void deleteTask(UUID projectId, UUID taskId) {
        taskRepository.deleteById(taskId);
        labelRepository.deleteUnusedLabels(projectId);
    }

    public UpdateTaskColumnResponseDTO updateTaskColumn(UpdateTaskColumnRequestDTO requestDTO, UUID taskId) {
        boolean ifReindexingNeeded = checkIfTaskPositionsInColumnNeedReindexing(
                requestDTO.positionInColumn(),
                requestDTO.nearestNeighboursPositionInColumn());

        // todo: reindexing!
        // reindexing()

        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .entityType("TaskEntity")
                        .identifier("id", taskId)
                        .build());

        ColumnEntity column = columnRepository.findById(requestDTO.targetColumnId())
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .entityType("ColumnEntity")
                        .identifier("id", requestDTO.targetColumnId())
                        .build());

        task.setColumn(column);
        task.setPositionInColumn(requestDTO.positionInColumn());

        TaskEntity savedTask = taskRepository.save(task);

        return UpdateTaskColumnMapper.INSTANCE.toUpdateTaskColumnResponseDTO(savedTask);
    }

    private boolean checkIfTaskPositionsInColumnNeedReindexing(Double positionInColumn, List<Double> nearestNeighboursPositionInColumn) {
        return nearestNeighboursPositionInColumn.stream()
                .map(neighbourPositionInColumn -> Math.abs(positionInColumn - neighbourPositionInColumn))
                .anyMatch(distance -> distance < MINIMUM_DISTANCE_BETWEEN_TASKS);
    }
}