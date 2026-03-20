--liquibase formatted sql

--changeset stodo:29 context:dev,prod
-- Add stage column to tasks table
ALTER TABLE tasks 
ADD COLUMN stage VARCHAR(10) NOT NULL DEFAULT 'BOARD'
CHECK (stage IN ('BACKLOG', 'BOARD', 'ARCHIVED'));

-- Add index on stage column
CREATE INDEX IF NOT EXISTS tasks_stage_idx ON tasks(stage);

--rollback DROP INDEX IF EXISTS tasks_stage_idx;
--rollback ALTER TABLE tasks DROP COLUMN stage;

--changeset stodo:30 context:dev,prod
-- Update existing tasks to have BOARD stage (default value already set to BOARD in DDL, this is just for existing data)
UPDATE tasks SET stage = 'BOARD' WHERE stage IS NULL OR stage = '';

--rollback UPDATE tasks SET stage = NULL;