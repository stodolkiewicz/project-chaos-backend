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
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u.project.id FROM UserEntity u WHERE u.email = :email")
    Optional<UUID> findDefaultProjectIdByEmail(@Email String email);

    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.email = :email AND u.project IS NOT NULL")
    boolean existsDefaultProjectByEmail(@Email String email);

    @Query("SELECT new com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO(u.email) " +
           "FROM UserEntity u " +
           "JOIN u.projectUsers pu " +
           "WHERE pu.project.id = :projectId")
    List<ProjectUserQueryResponseDTO> findProjectUsersByProjectId(UUID projectId);
    
    @Query("SELECT u.email FROM UserEntity u WHERE u.project.id = :projectId")
    List<String> findUserEmailsWithDefaultProject(@Param("projectId") UUID projectId);
    
    @Modifying
    @Query("UPDATE UserEntity u SET u.project = NULL WHERE u.project.id = :projectId")
    void clearDefaultProjectForProject(@Param("projectId") UUID projectId);
    
    @Modifying  
    @Query("UPDATE UserEntity u SET u.project.id = :projectId WHERE u.email = :email")
    void setDefaultProject(@Param("email") String email, @Param("projectId") UUID projectId);

    //    Update all users - set their new default project to the lowest ID project from those they have access to (but not the
    //            deleted project), but only for users who:
    //            1. Currently have the deleted project as default
    //            2. And simultaneously have access to some other project (i.e., have an alternative)
    @Modifying
    @Query("""
        UPDATE UserEntity u SET u.project.id = (
            SELECT MIN(pu.project.id) 
            FROM ProjectUsersEntity pu 
            WHERE pu.user.email = u.email 
            AND pu.project.id != :excludeProjectId
        )
        WHERE u.project.id = :excludeProjectId
        AND EXISTS (
            SELECT 1 FROM ProjectUsersEntity pu2 
            WHERE pu2.user.email = u.email 
            AND pu2.project.id != :excludeProjectId
        )
        """)
    void batchUpdateDefaultProjectsForUsersWithAlternatives(@Param("excludeProjectId") UUID excludeProjectId);

}
