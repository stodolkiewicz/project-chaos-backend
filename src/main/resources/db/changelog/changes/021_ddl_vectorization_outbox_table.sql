--liquibase formatted sql

--changeset stodo:39 context:dev,prod
CREATE TABLE vectorization_outbox (
    id UUID PRIMARY KEY,
    attachment_id UUID NOT NULL UNIQUE REFERENCES attachments(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'FAILED')),
    retry_count INT NOT NULL DEFAULT 0,
    created_date TIMESTAMP NOT NULL,
    processed_at TIMESTAMP
);
--rollback DROP TABLE vectorization_outbox;