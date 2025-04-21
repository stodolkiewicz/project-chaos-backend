package com.stodo.projectchaos.service;

import com.stodo.projectchaos.model.dto.response.UserProjectQueryResponseDTO;
import com.stodo.projectchaos.model.dto.response.UserProjectsResponseDTO;
import com.stodo.projectchaos.repository.CustomProjectRepository;
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
    private final UserRepository userRepository;

    public ProjectService(CustomProjectRepository customProjectRepository, UserRepository userRepository) {
        this.customProjectRepository = customProjectRepository;
        this.userRepository = userRepository;
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
