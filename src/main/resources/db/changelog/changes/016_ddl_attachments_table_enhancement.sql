--liquibase formatted sql

--changeset stodo:33 context:dev,prod
-- Add new columns to attachments table for enhanced file management

-- Add project_id column with FK to projects
ALTER TABLE attachments ADD COLUMN project_id UUID NOT NULL;
ALTER TABLE attachments ADD CONSTRAINT fk_attachments_project FOREIGN KEY (project_id) REFERENCES projects(id);

-- Add user_id column (keeping existing uploaded_by for compatibility)
ALTER TABLE attachments ADD COLUMN user_id UUID;
ALTER TABLE attachments ADD CONSTRAINT fk_attachments_user_id FOREIGN KEY (user_id) REFERENCES users(id);

-- Add file metadata columns
ALTER TABLE attachments ADD COLUMN file_size_in_bytes BIGINT;
ALTER TABLE attachments ADD COLUMN file_path VARCHAR(500);
ALTER TABLE attachments ADD COLUMN original_name VARCHAR(255);
ALTER TABLE attachments ADD COLUMN content_type VARCHAR(100);

-- Add extracted text column for document content
ALTER TABLE attachments ADD COLUMN extracted_text TEXT;

-- Add vector_status enum column
ALTER TABLE attachments ADD COLUMN vector_status VARCHAR(20) DEFAULT 'PENDING' CHECK (vector_status IN ('PENDING', 'PROCESSED', 'FAILED'));

-- Add storage_status enum column
ALTER TABLE attachments ADD COLUMN storage_status VARCHAR(20) NOT NULL DEFAULT 'SAVED' CHECK (storage_status IN ('PENDING_UPLOAD', 'SAVED'));

-- Drop old uploaded_by column (replaced by user_id)
ALTER TABLE attachments DROP CONSTRAINT fk_attachments_user;
ALTER TABLE attachments DROP COLUMN uploaded_by;

-- Drop file_url column (replaced by file_path)
ALTER TABLE attachments DROP COLUMN file_url;

-- Drop uploaded_at column (using created_date from Auditable)
ALTER TABLE attachments DROP COLUMN uploaded_at;

--rollback ALTER TABLE attachments ADD COLUMN uploaded_by UUID;
--rollback ALTER TABLE attachments ADD CONSTRAINT fk_attachments_user FOREIGN KEY (uploaded_by) REFERENCES users(id);
--rollback ALTER TABLE attachments ADD COLUMN file_url TEXT;
--rollback ALTER TABLE attachments ADD COLUMN uploaded_at TIMESTAMP;
--rollback ALTER TABLE attachments DROP CONSTRAINT fk_attachments_project;
--rollback ALTER TABLE attachments DROP CONSTRAINT fk_attachments_user_id;
--rollback ALTER TABLE attachments DROP COLUMN project_id;
--rollback ALTER TABLE attachments DROP COLUMN user_id;
--rollback ALTER TABLE attachments DROP COLUMN file_size_in_bytes;
--rollback ALTER TABLE attachments DROP COLUMN file_path;
--rollback ALTER TABLE attachments DROP COLUMN original_name;
--rollback ALTER TABLE attachments DROP COLUMN content_type;
--rollback ALTER TABLE attachments DROP COLUMN extracted_text;
--rollback ALTER TABLE attachments DROP COLUMN vector_status;
--rollback ALTER TABLE attachments DROP COLUMN storage_status;