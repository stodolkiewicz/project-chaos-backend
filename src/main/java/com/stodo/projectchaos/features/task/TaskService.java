package com.stodo.projectchaos.features.task;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.task.dto.request.CreateTaskRequestDTO;
import com.stodo.projectchaos.features.task.dto.service.BoardTask;
import com.stodo.projectchaos.features.task.dto.service.Task;
import com.stodo.projectchaos.features.task.dto.request.UpdateTaskColumnRequestDTO;
import com.stodo.projectchaos.features.task.dto.service.TaskColumnUpdate;
import com.stodo.projectchaos.features.task.dto.mapper.TaskEntityMapper;
import com.stodo.projectchaos.model.entity.ColumnEntity;
import com.stodo.projectchaos.model.entity.TaskEntity;
import com.stodo.projectchaos.model.entity.TaskComments;
import com.stodo.projectchaos.features.column.ColumnRepository;
import com.stodo.projectchaos.features.label.LabelRepository;
import com.stodo.projectchaos.features.taskcomments.TaskCommentsRepository;
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
    private final TaskCommentsRepository taskCommentsRepository;

    private static final Double MINIMUM_DISTANCE_BETWEEN_TASKS = 0.000001;

    public List<BoardTask> getBoardTasks(UUID projectId) {
        return customBoardRepository.findBoardTasks(projectId);
    }

    public Task createTask(CreateTaskRequestDTO requestDTO, UUID projectId) {
        return customTaskRepository.createTask(requestDTO, projectId);
    }

    public void deleteTask(UUID projectId, UUID taskId) {
        List<TaskComments> comments = taskCommentsRepository.findByTaskId(taskId);
        taskCommentsRepository.deleteAll(comments);
        
        taskRepository.deleteById(taskId);
        labelRepository.deleteUnusedLabels(projectId);
    }

    public TaskColumnUpdate updateTaskColumn(UpdateTaskColumnRequestDTO requestDTO, UUID taskId) {
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

        return TaskEntityMapper.INSTANCE.toTaskColumnUpdate(savedTask);
    }

    private boolean checkIfTaskPositionsInColumnNeedReindexing(Double positionInColumn, List<Double> nearestNeighboursPositionInColumn) {
        return nearestNeighboursPositionInColumn.stream()
                .map(neighbourPositionInColumn -> Math.abs(positionInColumn - neighbourPositionInColumn))
                .anyMatch(distance -> distance < MINIMUM_DISTANCE_BETWEEN_TASKS);
    }
}