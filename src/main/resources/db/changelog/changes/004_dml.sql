--liquibase formatted sql

--changeset stodo:16
INSERT INTO columns (
    id, name, position, project_id, version
) VALUES
    ('282ea613-d683-4891-ac07-baae779685a4', 'To Do', 0, 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 0),
    ('596ddab4-1af6-4c3a-967c-d48e64c90880', 'In Progress', 1, 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',  0),
    ('622adf67-7dce-45c0-9e7d-307a15c91e51', 'Done', 2, 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 0);
--rollback DELETE FROM columns WHERE id IN ('282ea613-d683-4891-ac07-baae779685a4','596ddab4-1af6-4c3a-967c-d48e64c90880', '622adf67-7dce-45c0-9e7d-307a15c91e51');

--changeset stodo:11
INSERT INTO task_priorities (
    id, priority_value, name, color, version
) VALUES
    ('d9a88e6d-e06c-4b50-9c89-11c0f2f7ef6f', 1, 'Low', '#A3BE8C', 0),
    ('db7edc9e-96e8-475d-a691-6f5365663a7f', 3, 'Medium', '#EBCB8B', 0),
    ('87b439e5-00a3-4674-b77c-e60a24ff76ca', 5, 'High', '#BF616A', 0);
--rollback DELETE FROM task_priorities WHERE id IN (
--rollback    'd9a88e6d-e06c-4b50-9c89-11c0f2f7ef6f',
--rollback    'db7edc9e-96e8-475d-a691-6f5365663a7f',
--rollback    '87b439e5-00a3-4674-b77c-e60a24ff76ca'
--rollback );

--changeset stodo:12
INSERT INTO tasks (
    id, title, description, position_in_column, assignee_email, column_id, priority_id,
    created_date, last_modified_date, last_modified_by, version
) VALUES
    ('8113faed-e1cd-4e34-a8cf-e3ec59c948d9', 'Write Docs', 'Write documentation for the new feature', 0,
     'projectchaos32167@gmail.com', '282ea613-d683-4891-ac07-baae779685a4', 'd9a88e6d-e06c-4b50-9c89-11c0f2f7ef6f',
     NOW(), NOW(), 'stodo', 0),
     ('8113faed-e1cd-4e34-a8cf-e3ec59c94fff', 'Write Docs 2', 'Write documentation for the newest feature', 1,
      'projectchaos32167@gmail.com', '282ea613-d683-4891-ac07-baae779685a4', 'd9a88e6d-e06c-4b50-9c89-11c0f2f7ef6f',
      NOW(), NOW(), 'stodo', 0),

    ('c7b1bfa6-a697-4631-8cbd-3f5b43b5cc0b', 'Implement Feature X', 'Code core logic for feature X', 0,
     'projectchaos32167@gmail.com', '596ddab4-1af6-4c3a-967c-d48e64c90880', '87b439e5-00a3-4674-b77c-e60a24ff76ca',
     NOW(), NOW(), 'stodo', 0),

    ('adbd6529-1b3d-4ae2-a6fb-63fce48f133d', 'Bug Fix #42', 'Fix crash when saving settings', 0,
     'projectchaos32167@gmail.com', '622adf67-7dce-45c0-9e7d-307a15c91e51', 'db7edc9e-96e8-475d-a691-6f5365663a7f',
     NOW(), NOW(), 'stodo', 0);
--rollback DELETE FROM tasks WHERE id IN ('8113faed-e1cd-4e34-a8cf-e3ec59c948d9', 'c7b1bfa6-a697-4631-8cbd-3f5b43b5cc0b', 'adbd6529-1b3d-4ae2-a6fb-63fce48f133d');