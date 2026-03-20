--liquibase formatted sql

--changeset stodo:27 context:dev,prod
-- Add stage column to tasks table
ALTER TABLE tasks 
ADD COLUMN stage VARCHAR(10) NOT NULL DEFAULT 'BOARD'
CHECK (stage IN ('BACKLOG', 'BOARD', 'DONE'));

-- Add index on stage column
CREATE INDEX IF NOT EXISTS tasks_stage_idx ON tasks(stage);

--rollback DROP INDEX IF EXISTS tasks_stage_idx;
--rollback ALTER TABLE tasks DROP COLUMN stage;