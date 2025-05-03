package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.model.entity.UserEntity;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u.project.id FROM UserEntity u WHERE u.email = :email")
    Optional<UUID> findDefaultProjectIdByEmail(@Email String email);

    @Query("SELECT new com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO(u.email) " +
           "FROM UserEntity u " +
           "JOIN u.projectUsers pu " +
           "WHERE pu.project.id = :projectId")
    List<ProjectUserQueryResponseDTO> findProjectUsersByProjectId(UUID projectId);

}
