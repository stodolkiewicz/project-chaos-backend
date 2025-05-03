--liquibase formatted sql

--changeset stodo:7
CREATE TABLE IF NOT EXISTS columns (
      id UUID PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      position SMALLINT,
      project_id UUID NOT NULL,

      version INTEGER NOT NULL DEFAULT 0,

      CONSTRAINT fk_columns_projects FOREIGN KEY (project_id) REFERENCES projects(id),
      CONSTRAINT unique_column_position_per_project UNIQUE (position, project_id)
);
--rollback drop table columns;

--changeset stodo:8
CREATE TABLE IF NOT EXISTS task_priorities (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    priority_value SMALLINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) NOT NULL,
    CHECK (priority_value BETWEEN 1 AND 5),
    version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_tasks_priorities_projects FOREIGN KEY (project_id) REFERENCES projects(id)
)
--rollback drop table task_priorities;

--changeset stodo:9
CREATE TABLE IF NOT EXISTS tasks (
      id UUID PRIMARY KEY,
      title VARCHAR(255) NOT NULL,
      description TEXT,
      position_in_column FLOAT8,
      assignee_email VARCHAR(100),
      column_id UUID,
      priority_id UUID,

    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(120),
    version INTEGER NOT NULL DEFAULT 0,

      CONSTRAINT fk_tasks_priority FOREIGN KEY (priority_id) REFERENCES task_priorities(id),
      CONSTRAINT fk_tasks_users FOREIGN KEY (assignee_email) REFERENCES users(email),

      CONSTRAINT fk_tasks_columns FOREIGN KEY (column_id) REFERENCES columns(id)
);
--rollback drop table tasks;

--changeset stodo:10
CREATE TABLE IF NOT EXISTS labels (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL,
    project_id UUID NOT NULL,

    version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_labels_projects FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT unique_label_per_project_and_color UNIQUE (name, project_id, color)
);
--rollback drop table labels;

--changeset stodo:11
CREATE TABLE IF NOT EXISTS task_labels (
    task_id UUID NOT NULL,
    label_id UUID NOT NULL,
    PRIMARY KEY (task_id, label_id),

        created_date TIMESTAMP NOT NULL,
        last_modified_date TIMESTAMP,
        last_modified_by VARCHAR(120),
        version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_task_labels_tasks FOREIGN KEY (task_id) REFERENCES tasks(id),
    CONSTRAINT fk_task_labels_labels FOREIGN KEY (label_id) REFERENCES labels(id)
);
--rollback drop table task_labels;

--changeset stodo:12
CREATE TABLE IF NOT EXISTS project_backlog (
    project_id UUID NOT NULL,
    task_id UUID NOT NULL,
    PRIMARY KEY (project_id, task_id),

    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(120),
    version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_project_backlog_projects FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_project_backlog_tasks FOREIGN KEY (task_id) REFERENCES tasks(id)
);

--changeset stodo:13
CREATE TABLE IF NOT EXISTS task_comments (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    author_email VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,

        created_date TIMESTAMP NOT NULL,
        last_modified_date TIMESTAMP,
        last_modified_by VARCHAR(120),
        version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_comments_tasks FOREIGN KEY (task_id) REFERENCES tasks(id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_email) REFERENCES users(email)
);
--rollback drop table task_comments;

--changeset stodo:15
CREATE TABLE IF NOT EXISTS attachments (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    uploaded_by VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,

        created_date TIMESTAMP NOT NULL,
        last_modified_date TIMESTAMP,
        last_modified_by VARCHAR(120),
        version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_attachments_task FOREIGN KEY (task_id) REFERENCES tasks(id),
    CONSTRAINT fk_attachments_user FOREIGN KEY (uploaded_by) REFERENCES users(email)
);
--rollback drop table attachments;