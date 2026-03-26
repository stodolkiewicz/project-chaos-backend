package com.stodo.projectchaos.integration.repository.helper;

import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import com.stodo.projectchaos.model.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@TestConfiguration
public class TcHelper {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public UUID createProject(String name) {
        UUID projectId = UUID.randomUUID();
        System.out.println("Creating project with UUID: " + projectId + " and name: " + name);
        
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update(
            "INSERT INTO projects (id, name, created_date, last_modified_date, version) VALUES (?, ?, ?, ?, ?)",
            projectId, name, now, now, 0
        );

        // Create project storage usage with default limit (600MB)
        UUID storageUsageId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO project_storage_usage (id, project_id, used_bytes, limit_bytes, created_date, last_modified_date, last_modified_by, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            storageUsageId, projectId, 0L, 629145600L, now, now, "test", 0
        );

        return projectId;
    }

    public void createUserWithDefaultProject(String email, UUID defaultProjectId) {
        UUID userId = UUID.randomUUID();
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update(
            "INSERT INTO users (id, email, first_name, last_name, role, last_login, default_project_id, account_non_expired, account_non_locked, credentials_non_expired, enabled, created_date, last_modified_date, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            userId, email, null, null, RoleEnum.ROLE_USER.name(), now, defaultProjectId, true, true, true, true, now, now, 0
        );

        // Create user storage usage with default limit (300MB)
        UUID userStorageUsageId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO user_storage_usage (id, user_id, used_bytes, limit_bytes, created_date, last_modified_date, last_modified_by, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            userStorageUsageId, userId, 0L, 314572800L, now, now, "test", 0
        );
    }

    public void assignUserToProject(String email, UUID projectId) {
        // First get the user ID from email
        UUID userId = jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE email = ?", 
            UUID.class, 
            email
        );
        
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update(
            "INSERT INTO project_users (user_id, project_id, project_role, created_date, last_modified_date, version) VALUES (?, ?, ?, ?, ?, ?)",
            userId, projectId, ProjectRoleEnum.MEMBER.name(), now, now, 0
        );
    }

}