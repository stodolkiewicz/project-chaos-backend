--liquibase formatted sql

--changeset stodo:1
CREATE TABLE IF NOT EXISTS projects (
      id UUID PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      description TEXT,
      created_date TIMESTAMP NOT NULL
);
--rollback drop table projects;

--changeset stodo:2
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
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

    CONSTRAINT fk_default_project FOREIGN KEY (default_project_id) REFERENCES projects(id) ON DELETE SET NULL
);
--rollback drop table users;

--changeset stodo:3
CREATE TABLE project_users (
   project_id UUID NOT NULL,
   user_id UUID NOT NULL,
   project_role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
   created_date TIMESTAMP NOT NULL,
   PRIMARY KEY (project_id, user_id),
   CONSTRAINT fk_project_user_project FOREIGN KEY (project_id)
       REFERENCES projects(id) ON DELETE CASCADE,
   CONSTRAINT fk_project_user_user FOREIGN KEY (user_id)
       REFERENCES users(id) ON DELETE CASCADE,
   CONSTRAINT chk_project_role CHECK (project_role IN ('ADMIN', 'MEMBER', 'VIEWER'))
);
CREATE INDEX idx_project_users_user_id ON project_users(user_id);
CREATE INDEX idx_project_users_project_id ON project_users(project_id);
CREATE INDEX idx_project_users_role ON project_users(project_role);
CREATE INDEX idx_project_users_project_id_role ON project_users(project_id, project_role);

--rollback DROP INDEX IF EXISTS idx_project_users_project_id_role;
--rollback DROP INDEX IF EXISTS idx_project_users_project_id;
--rollback DROP INDEX IF EXISTS idx_project_users_role;
--rollback DROP INDEX IF EXISTS idx_project_users_user_id;
--rollback DROP TABLE IF EXISTS project_users;
