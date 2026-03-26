--liquibase formatted sql

--changeset stodo:35 context:dev,prod
-- Create project_storage_usage table for tracking project storage usage

CREATE TABLE project_storage_usage (
    id UUID NOT NULL PRIMARY KEY,
    project_id UUID NOT NULL,
    used_bytes BIGINT NOT NULL DEFAULT 0,
    limit_bytes BIGINT NOT NULL DEFAULT 0,
    
    -- Auditing columns
    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 0,
    
    -- Foreign key constraint
    CONSTRAINT fk_project_storage_usage_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- Create index for performance
CREATE INDEX idx_project_storage_usage_project_id ON project_storage_usage(project_id);

--rollback DROP INDEX idx_project_storage_usage_project_id;
--rollback DROP TABLE project_storage_usage;