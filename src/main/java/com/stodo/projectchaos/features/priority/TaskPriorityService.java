package com.stodo.projectchaos.features.priority;

import com.stodo.projectchaos.features.priority.dto.service.TaskPriority;
import com.stodo.projectchaos.features.priority.dto.mapper.TaskPriorityEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskPriorityService {
    private final TaskPriorityRepository taskPriorityRepository;

    public List<TaskPriority> findTaskPrioritiesByProjectId(UUID projectId) {
        return taskPriorityRepository.findByProjectId(projectId)
                .stream()
                .map(TaskPriorityEntityMapper.INSTANCE::toTaskPriority)
                .toList();
    }
}