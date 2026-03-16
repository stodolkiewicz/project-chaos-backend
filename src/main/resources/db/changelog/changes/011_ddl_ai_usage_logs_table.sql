--liquibase formatted sql

--changeset stodo:27 context:dev,prod
CREATE TABLE IF NOT EXISTS AI_USAGE_LOGS (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL,
    conversation_id   VARCHAR(36) NOT NULL,
    model_id          VARCHAR(255) NOT NULL,
    prompt_tokens     INTEGER NOT NULL,
    completion_tokens INTEGER NOT NULL,
    total_tokens      INTEGER NOT NULL,
    request_id        VARCHAR(255) NOT NULL,
    latency_ms        BIGINT NOT NULL,
    created_date      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_usage_user_date ON AI_USAGE_LOGS (user_id, created_date);
--rollback DROP INDEX IF EXISTS idx_ai_usage_user_date;
--rollback DROP TABLE IF EXISTS AI_USAGE_LOGS;
