--liquibase formatted sql

--changeset stodo:46 context:dev,prod
-- Every priority in a project has a different value.
-- The check runs at commit, so one update can swap the values of two priorities.
ALTER TABLE task_priorities
    ADD CONSTRAINT uq_task_priorities_project_value UNIQUE (project_id, priority_value)
    DEFERRABLE INITIALLY DEFERRED;
--rollback ALTER TABLE task_priorities DROP CONSTRAINT uq_task_priorities_project_value;
