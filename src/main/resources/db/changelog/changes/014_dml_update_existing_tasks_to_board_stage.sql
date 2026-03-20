--liquibase formatted sql

--changeset stodo:28 context:dev,prod
-- Update existing tasks to have BOARD stage (default value already set to BOARD in DDL, this is just for existing data)
UPDATE tasks SET stage = 'BOARD' WHERE stage IS NULL OR stage = '';

--rollback UPDATE tasks SET stage = NULL;