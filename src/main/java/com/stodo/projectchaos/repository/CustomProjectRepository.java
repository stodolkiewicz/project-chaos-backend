package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.dto.project.list.query.UserProjectQueryResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.query.SimpleProjectQueryResponseDTO;
import com.stodo.projectchaos.model.dto.project.firstalternativeproject.query.UserAlternativeProjectQueryResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CustomProjectRepository {

    @PersistenceContext
    private EntityManager em;

    public List<UserProjectQueryResponseDTO> findProjectsByUserEmail(String email) {
        List<UserProjectQueryResponseDTO> userProjectQueryResponseDTOList =
                em.createQuery("""
                        select new com.stodo.projectchaos.model.dto.project.list.query.UserProjectQueryResponseDTO(
                            pu.project.id,
                            pu.project.name,
                            pu.project.description,
                            pu.project.createdDate,
                            pu.projectRole,
                            pu.createdDate
                        )
                        from ProjectUsersEntity pu
                        join pu.user u
                        where u.email = :email      
                        """, UserProjectQueryResponseDTO.class)
                .setParameter("email", email)
                .getResultList();

        return new ArrayList<>(userProjectQueryResponseDTOList);
    }

    public boolean isUserAdminInProject(String email, UUID projectId) {
        return em.createQuery("""
                    select COUNT(pu) > 0
                    from ProjectUsersEntity pu
                    where pu.user.email = :email and
                          pu.project.id = :projectId and
                          pu.projectRole = :role
                    """, Boolean.class)
                .setParameter("email", email)
                .setParameter("projectId", projectId)
                .setParameter("role", ProjectRoleEnum.ADMIN)
                .getSingleResult();
    }

    public boolean hasAtLeastMemberRole(String email, UUID projectId) {
        return em.createQuery("""
                    select COUNT(pu) > 0
                    from ProjectUsersEntity pu
                    where pu.user.email = :email and
                          pu.project.id = :projectId and
                          pu.projectRole in (:roles)
                    """, Boolean.class)
                .setParameter("email", email)
                .setParameter("projectId", projectId)
                .setParameter("roles", List.of(ProjectRoleEnum.MEMBER, ProjectRoleEnum.ADMIN))
                .getSingleResult();
    }

    public Optional<ProjectEntity> getDefaultProjectForUser(String email) {
        return em.createQuery("""
                        select u.project
                        from UserEntity u
                        where u.email = :email
                        """, ProjectEntity.class)
                        .setParameter("email", email)
                        .getResultStream()
                        .findFirst();
    }

    public List<SimpleProjectQueryResponseDTO> findSimpleProjectsByUserEmail(String email) {
        List<SimpleProjectQueryResponseDTO> simpleProjectQueryResponseDTOList =
                em.createQuery("""
                        select new com.stodo.projectchaos.model.dto.project.list.query.SimpleProjectQueryResponseDTO(
                            pu.project.id,
                            pu.project.name
                        )
                        from ProjectUsersEntity pu
                        join pu.user u
                        where u.email = :email      
                        """, SimpleProjectQueryResponseDTO.class)
                .setParameter("email", email)
                .getResultList();

        return new ArrayList<>(simpleProjectQueryResponseDTOList);
    }
    
    public List<UserAlternativeProjectQueryResponseDTO> findFirstAlternativeProjectForUsers(List<String> emails, UUID excludeProjectId) {
        if (emails.isEmpty()) {
            return new ArrayList<>();
        }
        
        return em.createQuery("""
            SELECT new com.stodo.projectchaos.model.dto.project.firstalternativeproject.query.UserAlternativeProjectQueryResponseDTO(
                pu.user.email, 
                MIN(pu.project.id)
            )
            FROM ProjectUsersEntity pu 
            WHERE pu.user.email IN :emails 
            AND pu.project.id != :excludeProjectId
            GROUP BY pu.user.email
            """, UserAlternativeProjectQueryResponseDTO.class)
            .setParameter("emails", emails)
            .setParameter("excludeProjectId", excludeProjectId)
            .getResultList();
    }

}