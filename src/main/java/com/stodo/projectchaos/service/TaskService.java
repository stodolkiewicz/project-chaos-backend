package com.stodo.projectchaos.service;

import com.stodo.projectchaos.model.dto.response.boardtasks.BoardTasksResponseDTO;
import com.stodo.projectchaos.repository.CustomBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final CustomBoardRepository customBoardRepository;

    public List<BoardTasksResponseDTO> getBoardTasks(UUID projectId) {
        return customBoardRepository.findBoardTasks(projectId);
    }
}