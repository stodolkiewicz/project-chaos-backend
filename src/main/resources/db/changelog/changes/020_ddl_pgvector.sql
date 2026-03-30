--liquibase formatted sql

--changeset stodo:38 context:dev,prod
-- Enable pgvector and create vector store table
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE attachment_embeddings (
       id            uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
       attachment_id uuid NOT NULL REFERENCES attachments(id) ON DELETE CASCADE,
       task_id       uuid REFERENCES tasks(id) ON DELETE CASCADE,
       project_id    uuid NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
       chunk_index   int  NOT NULL DEFAULT 0,
       content    text,
       metadata      jsonb,
       embedding     vector(1536) NOT NULL,
    -- Auditing columns
       created_date TIMESTAMP NOT NULL
);


CREATE INDEX ON attachment_embeddings USING HNSW (embedding vector_cosine_ops);
CREATE INDEX ON attachment_embeddings (project_id);
CREATE INDEX ON attachment_embeddings (task_id) WHERE task_id IS NOT NULL;
CREATE INDEX ON attachment_embeddings (attachment_id);
-- Metadata index (GIN for fast JSON searching)
CREATE INDEX idx_attachment_embeddings_metadata ON attachment_embeddings USING GIN (metadata);

--rollback DROP TABLE IF EXISTS attachment_embeddings;
--rollback DROP EXTENSION IF EXISTS vector;
--rollback DROP EXTENSION IF EXISTS hstore;
--rollback DROP EXTENSION IF EXISTS "uuid-ossp";