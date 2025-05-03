package com.stodo.projectchaos.service;

import com.stodo.projectchaos.model.dto.task.create.request.CreateTaskRequestDTO;
import com.stodo.projectchaos.model.dto.task.board.response.BoardTasksResponseDTO;
import com.stodo.projectchaos.model.dto.task.create.response.CreateTaskResponseDTO;
import com.stodo.projectchaos.repository.CustomBoardRepository;
import com.stodo.projectchaos.repository.CustomTaskRepository;
import com.stodo.projectchaos.repository.TaskRepository;
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

    public List<BoardTasksResponseDTO> getBoardTasks(UUID projectId) {
        return customBoardRepository.findBoardTasks(projectId);
    }

    public CreateTaskResponseDTO createTask(CreateTaskRequestDTO requestDTO, UUID projectId) {
        return customTaskRepository.createTask(requestDTO, projectId);
    }

    public void deleteTask(UUID taskId) {
        taskRepository.deleteById(taskId);
    }

}