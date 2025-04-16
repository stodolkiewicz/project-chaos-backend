package com.stodo.projectchaos.service;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.mapper.ProjectMapper;
import com.stodo.projectchaos.repository.CustomProjectRepository;
import com.stodo.projectchaos.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProjectService {

    private final CustomProjectRepository customProjectRepository;
    private final UserRepository userRepository;

    public ProjectService(CustomProjectRepository customProjectRepository, UserRepository userRepository) {
        this.customProjectRepository = customProjectRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectResponseDTO> findProjectsByUserEmail(String email) {
        CompletableFuture<List<ProjectEntity>> projectsFuture =
                CompletableFuture.supplyAsync(() -> customProjectRepository.findProjectsByUserEmail(email));

        CompletableFuture<Optional<UserEntity>> userFuture =
                CompletableFuture.supplyAsync(() -> userRepository.findByEmail(email));

        CompletableFuture.allOf(projectsFuture, userFuture).join();

        List<ProjectEntity> userProjects = projectsFuture.join();
        UserEntity user = userFuture.join()
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .entityType("User")
                        .identifier("email", email)
                        .build());

        UUID defaultProjectId = user.getProject() != null ? user.getProject().getId() : null;

        return userProjects.stream()
                .map(project -> ProjectMapper.toDto(project, defaultProjectId))
                .toList();
    }
}
