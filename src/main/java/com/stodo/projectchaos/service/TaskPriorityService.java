package com.stodo.projectchaos.service;

import com.stodo.projectchaos.mapper.TaskPriorityMapper;
import com.stodo.projectchaos.model.dto.response.boardtasks.PriorityDTO;
import com.stodo.projectchaos.model.dto.response.taskpriorities.TaskPriorityResponseDTO;
import com.stodo.projectchaos.model.entity.TaskPriorityEntity;
import com.stodo.projectchaos.repository.TaskPriorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskPriorityService {
    private final TaskPriorityRepository taskPriorityRepository;

    public List<TaskPriorityResponseDTO> findTaskPrioritiesByProjectId(UUID projectId) {
        return TaskPriorityMapper.INSTANCE.toTaskPriorityResponseDTOList(
                taskPriorityRepository.findByProjectId(projectId)
        );
    }
}