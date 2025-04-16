package com.stodo.projectchaos.service;

import com.stodo.projectchaos.model.dto.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.mapper.ProjectMapper;
import com.stodo.projectchaos.repository.CustomProjectRepository;
import com.stodo.projectchaos.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final CustomProjectRepository customProjectRepository;
    private final UserRepository userRepository;

    public ProjectService(CustomProjectRepository customProjectRepository, UserRepository userRepository) {
        this.customProjectRepository = customProjectRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectResponseDTO> findProjectsByUserEmail(String email) {
        List<ProjectEntity> userProjects = customProjectRepository.findProjectsByUserEmail(email);
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        UUID defaultProjectId = user.getProject() != null ? user.getProject().getId() : null;

        return userProjects.stream()
                .map(project -> ProjectMapper.toDto(project, defaultProjectId))
                .toList();
    }
}
