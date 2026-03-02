--liquibase formatted sql

--changeset stodo:22 context:dev,prod
-- Create invitations table for project invitations

CREATE TABLE invitations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'viewer', -- np. 'admin', 'editor', 'viewer' 
    invited_by UUID NOT NULL REFERENCES users(id),
    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    version INTEGER,
    
    -- Constraint ensuring unique invitation per email and project
    CONSTRAINT unique_invitation UNIQUE (email, project_id)
);

-- Index for faster lookups when checking invitations during login
CREATE INDEX idx_invitations_email ON invitations(email);

--rollback DROP TABLE invitations CASCADE;