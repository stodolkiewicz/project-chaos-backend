package com.stodo.projectchaos.service;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.user.update.request.ChangeDefaultProjectRequestDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.repository.ProjectRepository;
import com.stodo.projectchaos.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public UUID getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userEntity -> userEntity.getId())
                .orElseThrow(() -> EntityNotFoundException.builder()
                    .identifier("email", email)
                    .entityType("UserEntity")
                    .build());
    }

    public void changeDefaultProject(ChangeDefaultProjectRequestDTO request, String email) {
        CompletableFuture<UserEntity> userEntityCF = CompletableFuture.supplyAsync(
                () -> userRepository.findByEmail(email)
                        .orElseThrow(() -> EntityNotFoundException.builder()
                                .identifier("email", email)
                                .entityType("UserEntity")
                                .build()));

        CompletableFuture<ProjectEntity> projectEntityCF = CompletableFuture.supplyAsync(
                () -> projectRepository.findById(request.newDefaultProjectId())
                        .orElseThrow(() -> EntityNotFoundException.builder()
                                .identifier("projectId", request.newDefaultProjectId())
                                .entityType("ProjectEntity")
                                .build()));

        CompletableFuture<Void> cfs = CompletableFuture.allOf(userEntityCF, projectEntityCF);
        cfs.join();

        UserEntity userEntity = userEntityCF.join();
        ProjectEntity newDefaultProject = projectEntityCF.join();

        userEntity.setProject(newDefaultProject);
        userRepository.save(userEntity);
    }
}
