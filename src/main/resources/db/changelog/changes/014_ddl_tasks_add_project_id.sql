--liquibase formatted sql

--changeset stodo:31
-- Add project_id FK to tasks table for BACKLOG/ARCHIVED tasks support
ALTER TABLE tasks ADD COLUMN project_id UUID;

-- Update existing tasks to set project_id based on their column's project
UPDATE tasks 
SET project_id = (SELECT c.project_id FROM columns c WHERE c.id = tasks.column_id)
WHERE column_id IS NOT NULL AND project_id IS NULL;

-- Add NOT NULL constraint
ALTER TABLE tasks ALTER COLUMN project_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_project 
    FOREIGN KEY (project_id) REFERENCES projects(id);

-- Add index for performance
CREATE INDEX IF NOT EXISTS tasks_project_id_idx ON tasks(project_id);

--rollback DROP INDEX IF EXISTS tasks_project_id_idx;
--rollback ALTER TABLE tasks DROP CONSTRAINT IF EXISTS fk_tasks_project;
--rollback ALTER TABLE tasks DROP COLUMN project_id;