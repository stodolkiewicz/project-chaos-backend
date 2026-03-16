--liquibase formatted sql

--changeset stodo:28 context:dev
-- Update test users with fixed UUIDs for development environment

-- Temporarily disable foreign key constraints
ALTER TABLE project_users DROP CONSTRAINT IF EXISTS fk_project_users_user;
ALTER TABLE tasks DROP CONSTRAINT IF EXISTS fk_tasks_assignee;
ALTER TABLE task_comments DROP CONSTRAINT IF EXISTS fk_task_comments_author;
ALTER TABLE attachments DROP CONSTRAINT IF EXISTS fk_attachments_user;

-- Update foreign key references first (using old user IDs)
UPDATE project_users SET user_id = '96d9e09c-6cf0-4706-89ff-a12e1b8b8143' 
  WHERE user_id = (SELECT id FROM users WHERE email = 'projectchaos32167@gmail.com');
UPDATE project_users SET user_id = '74ba924e-bd27-4687-b1b2-2e113bb8fd10' 
  WHERE user_id = (SELECT id FROM users WHERE email = 'projectchaos32168@gmail.com');
UPDATE project_users SET user_id = 'fa3f4d44-e1a6-4930-8f3b-0cfd4a2dd44f' 
  WHERE user_id = (SELECT id FROM users WHERE email = 'projectchaos32169@gmail.com');
UPDATE project_users SET user_id = '937e885b-043f-404a-a964-19a85fa076f0' 
  WHERE user_id = (SELECT id FROM users WHERE email = 'stodoo@gmail.com');

UPDATE tasks SET assignee_id = '96d9e09c-6cf0-4706-89ff-a12e1b8b8143' 
  WHERE assignee_id = (SELECT id FROM users WHERE email = 'projectchaos32167@gmail.com');
UPDATE tasks SET assignee_id = '74ba924e-bd27-4687-b1b2-2e113bb8fd10' 
  WHERE assignee_id = (SELECT id FROM users WHERE email = 'projectchaos32168@gmail.com');
UPDATE tasks SET assignee_id = 'fa3f4d44-e1a6-4930-8f3b-0cfd4a2dd44f' 
  WHERE assignee_id = (SELECT id FROM users WHERE email = 'projectchaos32169@gmail.com');
UPDATE tasks SET assignee_id = '937e885b-043f-404a-a964-19a85fa076f0' 
  WHERE assignee_id = (SELECT id FROM users WHERE email = 'stodoo@gmail.com');

UPDATE task_comments SET author_id = '96d9e09c-6cf0-4706-89ff-a12e1b8b8143' 
  WHERE author_id = (SELECT id FROM users WHERE email = 'projectchaos32167@gmail.com');
UPDATE task_comments SET author_id = '74ba924e-bd27-4687-b1b2-2e113bb8fd10' 
  WHERE author_id = (SELECT id FROM users WHERE email = 'projectchaos32168@gmail.com');
UPDATE task_comments SET author_id = 'fa3f4d44-e1a6-4930-8f3b-0cfd4a2dd44f' 
  WHERE author_id = (SELECT id FROM users WHERE email = 'projectchaos32169@gmail.com');
UPDATE task_comments SET author_id = '937e885b-043f-404a-a964-19a85fa076f0' 
  WHERE author_id = (SELECT id FROM users WHERE email = 'stodoo@gmail.com');

UPDATE attachments SET uploaded_by = '96d9e09c-6cf0-4706-89ff-a12e1b8b8143' 
  WHERE uploaded_by = (SELECT id FROM users WHERE email = 'projectchaos32167@gmail.com');
UPDATE attachments SET uploaded_by = '74ba924e-bd27-4687-b1b2-2e113bb8fd10' 
  WHERE uploaded_by = (SELECT id FROM users WHERE email = 'projectchaos32168@gmail.com');
UPDATE attachments SET uploaded_by = 'fa3f4d44-e1a6-4930-8f3b-0cfd4a2dd44f' 
  WHERE uploaded_by = (SELECT id FROM users WHERE email = 'projectchaos32169@gmail.com');
UPDATE attachments SET uploaded_by = '937e885b-043f-404a-a964-19a85fa076f0' 
  WHERE uploaded_by = (SELECT id FROM users WHERE email = 'stodoo@gmail.com');

-- Update user IDs after foreign key references are updated
UPDATE users SET id = '96d9e09c-6cf0-4706-89ff-a12e1b8b8143' WHERE email = 'projectchaos32167@gmail.com';
UPDATE users SET id = '74ba924e-bd27-4687-b1b2-2e113bb8fd10' WHERE email = 'projectchaos32168@gmail.com';
UPDATE users SET id = 'fa3f4d44-e1a6-4930-8f3b-0cfd4a2dd44f' WHERE email = 'projectchaos32169@gmail.com';
UPDATE users SET id = '937e885b-043f-404a-a964-19a85fa076f0' WHERE email = 'stodoo@gmail.com';

-- Re-enable foreign key constraints
ALTER TABLE project_users ADD CONSTRAINT fk_project_users_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users(id);
ALTER TABLE task_comments ADD CONSTRAINT fk_task_comments_author FOREIGN KEY (author_id) REFERENCES users(id);
ALTER TABLE attachments ADD CONSTRAINT fk_attachments_user FOREIGN KEY (uploaded_by) REFERENCES users(id);

--rollback UPDATE users SET id = gen_random_uuid() WHERE email IN ('projectchaos32167@gmail.com', 'projectchaos32168@gmail.com', 'projectchaos32169@gmail.com', 'stodoo@gmail.com');