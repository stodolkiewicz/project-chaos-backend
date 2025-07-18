--liquibase formatted sql

--changeset stodo:4 context:dev
INSERT INTO projects (
    id, name, description, created_date, last_modified_date, last_modified_by, version
) VALUES
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'Project Chaos - Default', 'Default project for test user', NOW(), NOW(), 'stodo', 0),
    ('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'Project Chaos - Side', 'Second project for test user', NOW(), NOW(), 'stodo', 0);
--rollback DELETE FROM projects WHERE id IN ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e');

--changeset stodo:5 context:dev
INSERT INTO users (
    email, first_name, last_name, role, google_picture_link, last_login,
    account_non_expired, account_non_locked, credentials_non_expired, enabled, default_project_id,
    created_date, last_modified_date, last_modified_by, version
) VALUES 
    ('projectchaos32167@gmail.com',
    'Test',
    'User',
    'ROLE_USER',
    'https://lh3.googleusercontent.com/a/TEST_IMAGE_LINK',
    '2025-04-16 02:07:56.177856',
    true, true, true, true,
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    NOW(), NOW(), 'stodo', 0),
    ('projectchaos32168@gmail.com',
    'Test',
    'User',
    'ROLE_USER',
    'https://lh3.googleusercontent.com/a/TEST_IMAGE_LINK',
    '2025-04-16 02:07:56.177856',
    true, true, true, true,
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    NOW(), NOW(), 'stodo', 0),
    ('projectchaos32169@gmail.com',
    'Test',
    'User',
    'ROLE_USER',
    'https://lh3.googleusercontent.com/a/TEST_IMAGE_LINK',
    '2025-04-16 02:07:56.177856',
    true, true, true, true,
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    NOW(), NOW(), 'stodo', 0),
    ('stodoo@gmail.com',
    'firstname',
    'lastName',
    'ROLE_ADMIN',
    'https://lh3.googleusercontent.com/a/TEST_IMAGE_LINK',
    '2025-04-16 02:07:56.177856',
    true, true, true, true,
    null,
    NOW(), NOW(), 'stodo', 0);
--rollback DELETE FROM users WHERE email IN ('projectchaos32167@gmail.com', 'projectchaos32168@gmail.com', 'projectchaos32169@gmail.com', 'stodoo@gmail.com');

--changeset stodo:6 context:dev
INSERT INTO project_users (
    project_id, user_email, project_role, created_date, last_modified_date, last_modified_by, version
) VALUES
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'projectchaos32167@gmail.com', 'ADMIN', NOW(), NOW(), 'stodo', 0),
    ('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'projectchaos32167@gmail.com', 'MEMBER', NOW(), NOW(), 'stodo', 0),
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'projectchaos32168@gmail.com', 'MEMBER', NOW(), NOW(), 'stodo', 0),
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'projectchaos32169@gmail.com', 'MEMBER', NOW(), NOW(), 'stodo', 0);
--rollback DELETE FROM project_users WHERE project_role != 'deleteAll';
