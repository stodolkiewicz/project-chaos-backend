package com.stodo.projectchaos.features.task;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.task.dto.request.CreateTaskRequestDTO;
import com.stodo.projectchaos.features.task.dto.request.UpdateTaskRequestDTO;
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
import com.stodo.projectchaos.storage.AttachmentManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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
    private final AttachmentManager attachmentManager;

    private static final Double MINIMUM_DISTANCE_BETWEEN_TASKS = 0.000001;

    public List<BoardTask> getTasks(UUID projectId, TaskStageEnum stage) {
        if (stage == null) {
            return customBoardRepository.findAllTasks(projectId);
        } else if (stage == TaskStageEnum.BOARD) {
            return customBoardRepository.findSortedBoardTasksByPositionInColumn(projectId);
        }
        return customBoardRepository.findTasksByStage(projectId, stage);
    }

    public List<BoardTask> getTasks(UUID projectId, TaskStageEnum stage, UUID userId) {
        List<BoardTask> tasks = getTasks(projectId, stage);
        if (tasks.isEmpty()) {
            return tasks;
        }

        List<UUID> taskIds = tasks.stream()
                .map(BoardTask::getTaskId)
                .toList();

        Map<UUID, Long> unreadCommentsMap = taskCommentsRepository.countUnreadCommentsByTaskIds(taskIds, userId)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));

        tasks.forEach(task ->
                task.setUnreadCommentsCount(unreadCommentsMap.getOrDefault(task.getTaskId(), 0L))
        );

        return tasks;
    }

    public Task findTask(UUID projectId, UUID taskId) {
        TaskEntity task = validateTasksBelongToProject(List.of(taskId), projectId).get(0);
        return TaskEntityMapper.INSTANCE.toTask(task);
    }

    public Task createTask(CreateTaskRequestDTO requestDTO, UUID projectId) {
        return customTaskRepository.createTask(requestDTO, projectId);
    }

    public void updateTask(UUID projectId, UUID taskId, UpdateTaskRequestDTO requestDTO) {
        customTaskRepository.updateTask(projectId, taskId, requestDTO);
    }

    public void deleteTask(UUID projectId, UUID taskId) {
        List<TaskComments> comments = taskCommentsRepository.findByTaskId(taskId);
        taskCommentsRepository.deleteAll(comments);
        
        attachmentManager.deleteTaskFiles(projectId, taskId);
        
        taskRepository.deleteById(taskId);
        labelRepository.deleteUnusedLabels(projectId);

        log.info("Task deleted [taskId={}, projectId={}]",
                taskId,
                projectId);
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