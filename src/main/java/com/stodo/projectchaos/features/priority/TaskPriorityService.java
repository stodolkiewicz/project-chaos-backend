package com.stodo.projectchaos.features.priority;

import com.stodo.projectchaos.features.priority.dto.mapper.TaskPriorityMapper;
import com.stodo.projectchaos.features.priority.dto.response.TaskPriorityResponseDTO;
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