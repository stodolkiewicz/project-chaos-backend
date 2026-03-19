package com.stodo.projectchaos.features.user;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.user.dto.mapper.UserEntityMapper;
import com.stodo.projectchaos.features.user.dto.request.ChangeDefaultProjectRequestDTO;
import com.stodo.projectchaos.features.user.dto.service.User;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.features.project.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public Optional<UUID> findDefaultProjectIdByEmail(String email) {
        return userRepository.findDefaultProjectIdByEmail(email);
    }

    public UUID getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userEntity -> userEntity.getId())
                .orElseThrow(() -> EntityNotFoundException.builder()
                    .identifier("email", email)
                    .entityType("UserEntity")
                    .build());
    }

    public User getUserById(UUID userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("id", userId)
                        .entityType("UserEntity")
                        .build());
        
        return UserEntityMapper.INSTANCE.toUser(userEntity);
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
