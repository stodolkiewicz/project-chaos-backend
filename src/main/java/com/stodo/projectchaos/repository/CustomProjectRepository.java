package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.dto.response.UserProjectQueryResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomProjectRepository {

    @PersistenceContext
    private EntityManager em;

    public List<UserProjectQueryResponseDTO> findProjectsByUserEmail(String email) {
        List<UserProjectQueryResponseDTO> userProjectQueryResponseDTOList =
                em.createQuery("""
                        select new com.stodo.projectchaos.model.dto.response.UserProjectQueryResponseDTO(
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

}