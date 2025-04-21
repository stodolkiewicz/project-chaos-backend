--liquibase formatted sql

--changeset stodo:7
CREATE TABLE IF NOT EXISTS columns (
      id UUID PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      position SMALLINT,
      project_id UUID,
      created_date TIMESTAMP NOT NULL,
      last_modified_date TIMESTAMP,

--    on project deletion, related columns will be deleted
      CONSTRAINT fk_columns_projects FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
--rollback drop table columns;

--changeset stodo:8
CREATE TABLE IF NOT EXISTS tasks (
      id UUID PRIMARY KEY,
      title VARCHAR(255) NOT NULL,
      description TEXT,
      position SMALLINT,
      assignee_id UUID,
      column_id UUID,
      created_date TIMESTAMP NOT NULL,
      last_modified_date TIMESTAMP,

      CONSTRAINT fk_tasks_users FOREIGN KEY (assignee_id) REFERENCES users(id),
--    on column deletion, related tasks will be deleted
      CONSTRAINT fk_tasks_columns FOREIGN KEY (column_id) REFERENCES columns(id) ON DELETE CASCADE
);
--rollback drop table tasks;

--changeset stodo:9
CREATE TABLE IF NOT EXISTS labels (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);
--rollback drop table labels;

--changeset stodo:10
CREATE TABLE IF NOT EXISTS task_labels (
    task_id UUID NOT NULL,
    label_id UUID NOT NULL,

    PRIMARY KEY (task_id, label_id),

    CONSTRAINT fk_task_labels_tasks FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_labels_labels FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE
);
--rollback drop table task_labels;