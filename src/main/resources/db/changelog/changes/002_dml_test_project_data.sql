--liquibase formatted sql

--changeset stodo:4
INSERT INTO projects (id, name, description, created_date) VALUES
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'Project Chaos - Default', 'Default project for test user', NOW()),
    ('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'Project Chaos - Side', 'Second project for test user', NOW());
--rollback DELETE FROM projects WHERE id IN ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e');

--changeset stodo:5
INSERT INTO users (
    id, email, first_name, last_name, role, google_picture_link, last_login,
    account_non_expired, account_non_locked, credentials_non_expired, enabled, default_project_id
) VALUES (
             '3a2b1c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d',
             'projectchaos32167@gmail.com',
             'Test',
             'User',
             'ROLE_USER',
             'https://lh3.googleusercontent.com/a/TEST_IMAGE_LINK',
             '2025-04-16 02:07:56.177856',
             true, true, true, true,
             'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
         );
--rollback DELETE FROM users WHERE id = '3a2b1c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d';

--changeset stodo:6
INSERT INTO project_users (project_id, user_id, project_role, created_date) VALUES
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', '3a2b1c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d', 'ADMIN', NOW()),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', '3a2b1c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d', 'MEMBER', NOW());
--rollback DELETE FROM project_users WHERE user_id = '3a2b1c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d';