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
import com.stodo.projectchaos.model.enums.TaskStageEnum;
import com.stodo.projectchaos.features.column.ColumnRepository;
import com.stodo.projectchaos.features.label.LabelRepository;
import com.stodo.projectchaos.features.taskcomments.TaskCommentsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
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

    public List<BoardTask> getTasks(UUID projectId, TaskStageEnum stage) {
        if (stage == null) {
            return customBoardRepository.findAllTasks(projectId);
        } else if (stage == TaskStageEnum.BOARD) {
            return customBoardRepository.findSortedBoardTasksByPositionInColumn(projectId);
        }
        return customBoardRepository.findTasksByStage(projectId, stage);
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
        task.setStage(TaskStageEnum.BOARD);

        TaskEntity savedTask = taskRepository.save(task);

        return TaskEntityMapper.INSTANCE.toTaskColumnUpdate(savedTask);
    }

    public void moveTasksToBacklog(List<UUID> taskIds, UUID projectId) {
        List<TaskEntity> tasks = validateTasksBelongToProject(taskIds, projectId);
        
        for (TaskEntity task : tasks) {
            task.setStage(TaskStageEnum.BACKLOG);
            task.setColumn(null);
            task.setPositionInColumn(null);
        }
        
        taskRepository.saveAll(tasks);
    }

    public void moveTasksToArchive(List<UUID> taskIds, UUID projectId) {
        List<TaskEntity> tasks = validateTasksBelongToProject(taskIds, projectId);
        
        for (TaskEntity task : tasks) {
            task.setStage(TaskStageEnum.ARCHIVED);
            task.setColumn(null);
            task.setPositionInColumn(null);
        }
        
        taskRepository.saveAll(tasks);
    }

    public void moveTasksToBoard(List<UUID> taskIds, UUID projectId) {
        List<TaskEntity> tasks = validateTasksBelongToProject(taskIds, projectId);

        List<ColumnEntity> columns = columnRepository.findByProjectIdOrderByPosition(projectId);
        if (columns.isEmpty()) {
            throw EntityNotFoundException.builder()
                    .entityType("ColumnEntity")
                    .identifier("projectId", projectId)
                    .build();
        }
        
        ColumnEntity firstColumn = columns.get(0);
        
        Double maxPosition = taskRepository.findMaxPositionInColumnByColumnId(firstColumn.getId());
        double nextPosition = (maxPosition != null ? maxPosition : 0.0) + 1.0;
        
        for (TaskEntity task : tasks) {
            task.setStage(TaskStageEnum.BOARD);
            task.setColumn(firstColumn);
            task.setPositionInColumn(nextPosition++);
        }
        
        taskRepository.saveAll(tasks);
    }

    private List<TaskEntity> validateTasksBelongToProject(List<UUID> taskIds, UUID projectId) {
        List<TaskEntity> tasks = taskRepository.findAllByIdInAndProjectId(taskIds, projectId);
        
        if (tasks.size() != taskIds.size()) {
            throw EntityNotFoundException.builder()
                    .entityType("TaskEntity")
                    .identifier("some task IDs", "not found or not in project")
                    .build();
        }
        
        return tasks;
    }

    private boolean checkIfTaskPositionsInColumnNeedReindexing(Double positionInColumn, List<Double> nearestNeighboursPositionInColumn) {
        return nearestNeighboursPositionInColumn.stream()
                .map(neighbourPositionInColumn -> Math.abs(positionInColumn - neighbourPositionInColumn))
                .anyMatch(distance -> distance < MINIMUM_DISTANCE_BETWEEN_TASKS);
    }
}