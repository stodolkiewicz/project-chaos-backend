--liquibase formatted sql

--changeset stodo:43 context:dev,prod
-- Stores when a user last opened the comments of a task.
-- Unread are those comments of other users that were created after last_read_at.
CREATE TABLE IF NOT EXISTS task_comment_reads (
    user_id UUID NOT NULL,
    task_id UUID NOT NULL,
    last_read_at TIMESTAMP NOT NULL,

    PRIMARY KEY (user_id, task_id),
    CONSTRAINT fk_task_comment_reads_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_comment_reads_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);
--rollback DROP TABLE IF EXISTS task_comment_reads;
