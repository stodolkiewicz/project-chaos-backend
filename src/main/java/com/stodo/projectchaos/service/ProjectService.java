package com.stodo.projectchaos.service;

import com.stodo.projectchaos.model.dto.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.mapper.ProjectMapper;
import com.stodo.projectchaos.repository.CustomProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final CustomProjectRepository customProjectRepository;

    public ProjectService(CustomProjectRepository customProjectRepository) {
        this.customProjectRepository = customProjectRepository;
    }

    public List<ProjectResponseDTO> findProjectsByUserEmail(String email) {
        List<ProjectEntity> entities = customProjectRepository.findProjectsByUserEmail(email);
        return entities.stream()
                .map(ProjectMapper.MAPPER::toDto)
                .toList();
    }
}
