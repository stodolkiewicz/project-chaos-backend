--liquibase formatted sql

--changeset stodo:34 context:dev,prod
-- Create user_storage_usage table for tracking user storage usage

CREATE TABLE user_storage_usage (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    used_bytes BIGINT NOT NULL DEFAULT 0,
    limit_bytes BIGINT NOT NULL DEFAULT 0,
    
    -- Auditing columns
    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 0,
    
    -- Foreign key constraint
    CONSTRAINT fk_user_storage_usage_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for performance
CREATE INDEX idx_user_storage_usage_user_id ON user_storage_usage(user_id);

--rollback DROP INDEX idx_user_storage_usage_user_id;
--rollback DROP TABLE user_storage_usage;