--liquibase formatted sql

--changeset stodo:21 context:dev,prod
-- Change user primary key from email (varchar) to id (uuid)
-- And update all foreign key references

-- Step 1: Add new UUID id column to users table
ALTER TABLE users ADD COLUMN id UUID DEFAULT gen_random_uuid() NOT NULL;

-- Step 2: Add new UUID foreign key columns to related tables
ALTER TABLE project_users ADD COLUMN user_id UUID;
ALTER TABLE tasks ADD COLUMN assignee_id UUID;
ALTER TABLE task_comments ADD COLUMN author_id UUID;

-- Step 3: Populate new UUID columns based on existing email relationships
UPDATE project_users SET user_id = u.id FROM users u WHERE project_users.user_email = u.email;
UPDATE tasks SET assignee_id = u.id FROM users u WHERE tasks.assignee_email = u.email;
UPDATE task_comments SET author_id = u.id FROM users u WHERE task_comments.author_email = u.email;

-- Step 4: Drop old foreign key constraints
ALTER TABLE project_users DROP CONSTRAINT IF EXISTS fk_project_users_user;
ALTER TABLE tasks DROP CONSTRAINT IF EXISTS fk_tasks_assignee;
ALTER TABLE task_comments DROP CONSTRAINT IF EXISTS fk_task_comments_author;
ALTER TABLE attachments DROP CONSTRAINT IF EXISTS fk_attachments_user;

-- Step 5: Drop old email-based foreign key columns
ALTER TABLE project_users DROP COLUMN user_email;
ALTER TABLE tasks DROP COLUMN assignee_email;
ALTER TABLE task_comments DROP COLUMN author_email;

-- Step 6: Rename new UUID columns to final names
ALTER TABLE project_users RENAME COLUMN user_id TO user_id_final;
ALTER TABLE project_users RENAME COLUMN user_id_final TO user_id;

-- Step 7: Make new UUID columns NOT NULL where appropriate
ALTER TABLE project_users ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE task_comments ALTER COLUMN author_id SET NOT NULL;

-- Step 8: Drop old email primary key and make id the new primary key
ALTER TABLE users DROP CONSTRAINT users_pkey;
ALTER TABLE users ADD CONSTRAINT users_pkey PRIMARY KEY (id);
ALTER TABLE users ADD CONSTRAINT users_email_unique UNIQUE (email);

-- Step 9: Change uploaded_by column type to UUID and add new foreign key constraints
ALTER TABLE attachments ALTER COLUMN uploaded_by TYPE UUID USING uploaded_by::UUID;
ALTER TABLE project_users ADD CONSTRAINT fk_project_users_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users(id);
ALTER TABLE task_comments ADD CONSTRAINT fk_task_comments_author FOREIGN KEY (author_id) REFERENCES users(id);
ALTER TABLE attachments ADD CONSTRAINT fk_attachments_user FOREIGN KEY (uploaded_by) REFERENCES users(id);

--rollback DROP TABLE users CASCADE;