--liquibase formatted sql

--changeset stodo:42 context:dev,prod
-- The task stage is stored in tasks.stage, so the project_backlog table is not needed.
DROP TABLE IF EXISTS project_backlog;
--rollback CREATE TABLE IF NOT EXISTS project_backlog (
--rollback     project_id UUID NOT NULL,
--rollback     task_id UUID NOT NULL,
--rollback     PRIMARY KEY (project_id, task_id),
--rollback     created_date TIMESTAMP NOT NULL,
--rollback     last_modified_date TIMESTAMP,
--rollback     last_modified_by VARCHAR(120),
--rollback     version INTEGER NOT NULL DEFAULT 0,
--rollback     CONSTRAINT fk_project_backlog_projects FOREIGN KEY (project_id) REFERENCES projects(id),
--rollback     CONSTRAINT fk_project_backlog_tasks FOREIGN KEY (task_id) REFERENCES tasks(id)
--rollback );
