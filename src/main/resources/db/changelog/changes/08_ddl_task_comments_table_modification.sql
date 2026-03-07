--liquibase formatted sql

--changeset stodo:23 context:dev,prod
-- Modify task_comments_table - change content to JSONB
ALTER TABLE task_comments DROP COLUMN content;
ALTER TABLE task_comments ADD COLUMN content JSONB NOT NULL;
--rollback ALTER TABLE task_comments DROP COLUMN content;
--rollback ALTER TABLE task_comments ADD COLUMN content TEXT NOT NULL;

--changeset stodo:24 context:dev,prod
-- Modify task_comments_table - add reply_to
ALTER TABLE task_comments
    ADD COLUMN reply_to UUID CONSTRAINT fk_task_comments_task_comments references task_comments(id);
--rollback ALTER TABLE task_comments DROP COLUMN reply_to;