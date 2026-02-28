package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.model.entity.UserEntity;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u.project.id FROM UserEntity u WHERE u.email = :email")
    Optional<UUID> findDefaultProjectIdByEmail(@Email String email);

    @Query("SELECT new com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO(u.email, u.firstName, u.lastName, CAST(pu.projectRole AS string), u.googlePictureLink, pu.createdDate) " +
           "FROM UserEntity u " +
           "JOIN u.projectUsers pu " +
           "WHERE pu.project.id = :projectId")
    List<ProjectUserQueryResponseDTO> findProjectUsersByProjectId(UUID projectId);

    // ---- These 2 queries are used (consecutively) to handle users' default project id,
    //      when their default project is deleted ----------------------------------------------------------------------
    //
    //    Update all users - set their new default project to the max ID project (=random) from those they have access
    //    to (but not the deleted project), but only for users who:
    //            1. Currently have the deleted project as default
    //            2. And simultaneously have access to some other project (i.e., have an alternative)
    @Modifying(clearAutomatically = true) // true == clear the underlying persistence context after executing the modifying query.
    @Query("""
        UPDATE UserEntity u SET u.project.id = (
            SELECT MAX(pu.project.id) 
            FROM ProjectUsersEntity pu 
            WHERE pu.user.id = u.id 
            AND pu.project.id != :projectIdBeingDeleted
        )
        WHERE u.project.id = :projectIdBeingDeleted
        AND EXISTS (
            SELECT 1 FROM ProjectUsersEntity pu2 
            WHERE pu2.user.id = u.id 
            AND pu2.project.id != :projectIdBeingDeleted
        )
        """)
    int batchUpdateDefaultProjectsForUsersWithAlternatives(@Param("projectIdBeingDeleted") UUID projectIdBeingDeleted);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserEntity u SET u.project = NULL WHERE u.project.id = :projectId")
    void clearDefaultProjectForProject(@Param("projectId") UUID projectId);
    // ----------------------------------------------------------------------------------------------------------------
}
