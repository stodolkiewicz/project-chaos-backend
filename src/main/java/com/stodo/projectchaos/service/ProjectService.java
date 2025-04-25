package com.stodo.projectchaos.service;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.mapper.ProjectMapper;
import com.stodo.projectchaos.model.dto.response.DefaultProjectResponseDTO;
import com.stodo.projectchaos.model.dto.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.dto.response.UserProjectQueryResponseDTO;
import com.stodo.projectchaos.model.dto.response.UserProjectsResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.repository.CustomProjectRepository;
import com.stodo.projectchaos.repository.ProjectRepository;
import com.stodo.projectchaos.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProjectService {

    private final CustomProjectRepository customProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(CustomProjectRepository customProjectRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.customProjectRepository = customProjectRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public DefaultProjectResponseDTO getDefaultProjectForUser(String email) {
        return customProjectRepository.getDefaultProjectForUser(email)
                .map(DefaultProjectResponseDTO::of)
                .orElse(DefaultProjectResponseDTO.empty());
    }

    public ProjectResponseDTO findProjectById(UUID projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectMapper.INSTANCE::toProjectResponseDTO)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("projectId", projectId)
                        .entityType("ProjectEntity")
                        .build());
    }

    public UserProjectsResponseDTO findProjectsByUserEmail(String email) {

        CompletableFuture<List<UserProjectQueryResponseDTO>> userProjectsCF = CompletableFuture.supplyAsync(
                () -> customProjectRepository.findProjectsByUserEmail(email));

        CompletableFuture<Optional<UUID>> defaultUserProjectCF = CompletableFuture.supplyAsync(
                () -> userRepository.findDefaultProjectIdByEmail(email)
        );

        CompletableFuture<Void> cfs = CompletableFuture.allOf(userProjectsCF, defaultUserProjectCF);
        cfs.join();

        List<UserProjectQueryResponseDTO> projects = userProjectsCF.join();
        Optional<UUID> defaultProjectId = defaultUserProjectCF.join();

        moveDefaultProjectToFront(defaultProjectId, projects);

        return new UserProjectsResponseDTO(projects, defaultProjectId.orElse(null));
    }

    private static void moveDefaultProjectToFront(Optional<UUID> defaultProjectId, List<UserProjectQueryResponseDTO> projects) {
        defaultProjectId.ifPresent(id -> {
            Optional<UserProjectQueryResponseDTO> defaultProject = projects.stream()
                    .filter(project -> project.projectId().equals(id))
                    .findFirst();

            List<UserProjectQueryResponseDTO> mutableProjects = new ArrayList<>(projects);
            defaultProject.ifPresent(project -> {
                mutableProjects.remove(project);
                mutableProjects.addFirst(project);

            });
        });
    }
}
