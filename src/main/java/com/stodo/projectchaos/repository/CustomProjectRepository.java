package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.ProjectEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomProjectRepository {

    @PersistenceContext
    private EntityManager em;

    public List<ProjectEntity> findProjectsByUserEmail(String email) {
        return em.createQuery("""
                select pu.project from ProjectUsersEntity pu 
                JOIN pu.user u 
                WHERE u.email = :email          
            """, ProjectEntity.class)
        .setParameter("email", email)
        .getResultList();
    }

}