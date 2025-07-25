--liquibase formatted sql

--changeset stodo:1 context:dev,prod
CREATE TABLE IF NOT EXISTS projects (
      id UUID PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      description TEXT,

      created_date TIMESTAMP NOT NULL,
      last_modified_date TIMESTAMP,
      last_modified_by VARCHAR(120),
      version INTEGER NOT NULL DEFAULT 0
);
--rollback drop table projects;

--changeset stodo:2 context:dev,prod
CREATE TABLE IF NOT EXISTS users (
    email VARCHAR(100) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    google_picture_link VARCHAR(255),
    last_login TIMESTAMP,
    account_non_expired BOOLEAN NOT NULL DEFAULT true,
    account_non_locked BOOLEAN NOT NULL DEFAULT true,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT true,
    enabled BOOLEAN NOT NULL DEFAULT true,
    default_project_id UUID,

    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(120),
    version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_default_project FOREIGN KEY (default_project_id) REFERENCES projects(id)
);
--rollback drop table users;

--changeset stodo:3 context:dev,prod
CREATE TABLE project_users (
    project_id UUID NOT NULL,
    user_email VARCHAR(100) NOT NULL,
    project_role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',

    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(120),
    version INTEGER NOT NULL DEFAULT 0,

    PRIMARY KEY (project_id, user_email),
    CONSTRAINT fk_project_user_project FOREIGN KEY (project_id)
        REFERENCES projects(id),
    CONSTRAINT fk_project_user_user FOREIGN KEY (user_email)
        REFERENCES users(email),
    CONSTRAINT chk_project_role CHECK (project_role IN ('ADMIN', 'MEMBER', 'VIEWER'))
);
CREATE INDEX idx_project_users_email ON project_users(user_email);
CREATE INDEX idx_project_users_project_id ON project_users(project_id);
CREATE INDEX idx_project_users_project_id_role ON project_users(project_id, project_role);

--rollback DROP INDEX IF EXISTS idx_project_users_project_id_role;
--rollback DROP INDEX IF EXISTS idx_project_users_project_id;
--rollback DROP INDEX IF EXISTS idx_project_users_email;
--rollback DROP TABLE IF EXISTS project_users;
