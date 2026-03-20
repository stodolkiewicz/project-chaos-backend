--liquibase formatted sql

--changeset stodo:32 context:dev
-- Add test tasks with BACKLOG and ARCHIVED stages for Project Chaos - Default
INSERT INTO tasks (
    id, title, description, stage, position_in_column, assignee_id, column_id, priority_id, project_id,
    created_date, last_modified_date, last_modified_by, version
) VALUES
    -- BACKLOG tasks (no column_id needed for backlog)
    ('f1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c', 'Research API Integration', 'Research third-party API options for payment processing', 'BACKLOG', NULL,
     '96d9e09c-6cf0-4706-89ff-a12e1b8b8143', NULL, 'db7edc9e-96e8-475d-a691-6f5365663a7f', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
     NOW(), NOW(), 'stodo', 0),
    
    ('f2b3c4d5-e6f7-8a9b-0c1d-2e3f4a5b6c7d', 'Setup CI/CD Pipeline', 'Configure automated deployment pipeline for staging environment', 'BACKLOG', NULL,
     '74ba924e-bd27-4687-b1b2-2e113bb8fd10', NULL, '87b439e5-00a3-4674-b77c-e60a24ff76ca', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
     NOW(), NOW(), 'stodo', 0),
     
    ('f3c4d5e6-f7a8-9b0c-1d2e-3f4a5b6c7d8e', 'Database Optimization', 'Analyze and optimize slow database queries', 'BACKLOG', NULL,
     'fa3f4d44-e1a6-4930-8f3b-0cfd4a2dd44f', NULL, 'db7edc9e-96e8-475d-a691-6f5365663a7f', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
     NOW(), NOW(), 'stodo', 0),
     
    ('f4d5e6f7-a8b9-0c1d-2e3f-4a5b6c7d8e9f', 'Security Audit', 'Perform comprehensive security review of authentication system', 'BACKLOG', NULL,
     '96d9e09c-6cf0-4706-89ff-a12e1b8b8143', NULL, '87b439e5-00a3-4674-b77c-e60a24ff76ca', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
     NOW(), NOW(), 'stodo', 0),

    -- ARCHIVED tasks (assigned to Done column but no position_in_column)
    ('f5e6f7a8-b9c0-1d2e-3f4a-5b6c7d8e9f0a', 'Setup Development Environment', 'Configure local development environment with Docker', 'ARCHIVED', NULL,
     '96d9e09c-6cf0-4706-89ff-a12e1b8b8143', '622adf67-7dce-45c0-9e7d-307a15c91e51', 'd9a88e6d-e06c-4b50-9c89-11c0f2f7ef6f', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
     NOW(), NOW(), 'stodo', 0),
     
    ('f6f7a8b9-c0d1-2e3f-4a5b-6c7d8e9f0a1b', 'User Authentication Module', 'Implement JWT-based user authentication', 'ARCHIVED', NULL,
     '74ba924e-bd27-4687-b1b2-2e113bb8fd10', '622adf67-7dce-45c0-9e7d-307a15c91e51', '87b439e5-00a3-4674-b77c-e60a24ff76ca', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
     NOW(), NOW(), 'stodo', 0),
     
    ('f7a8b9c0-d1e2-3f4a-5b6c-7d8e9f0a1b2c', 'Project Structure Setup', 'Create base project structure and configure Maven dependencies', 'ARCHIVED', NULL,
     'fa3f4d44-e1a6-4930-8f3b-0cfd4a2dd44f', '622adf67-7dce-45c0-9e7d-307a15c91e51', 'd9a88e6d-e06c-4b50-9c89-11c0f2f7ef6f', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
     NOW(), NOW(), 'stodo', 0);

--rollback DELETE FROM tasks WHERE id IN (
--rollback    'f1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c',
--rollback    'f2b3c4d5-e6f7-8a9b-0c1d-2e3f4a5b6c7d',
--rollback    'f3c4d5e6-f7a8-9b0c-1d2e-3f4a5b6c7d8e',
--rollback    'f4d5e6f7-a8b9-0c1d-2e3f-4a5b6c7d8e9f',
--rollback    'f5e6f7a8-b9c0-1d2e-3f4a-5b6c7d8e9f0a',
--rollback    'f6f7a8b9-c0d1-2e3f-4a5b-6c7d8e9f0a1b',
--rollback    'f7a8b9c0-d1e2-3f4a-5b6c-7d8e9f0a1b2c'
--rollback );