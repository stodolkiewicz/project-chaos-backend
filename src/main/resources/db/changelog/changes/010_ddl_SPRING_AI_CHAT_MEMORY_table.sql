--liquibase formatted sql

--changeset stodo:26 context:dev,prod
-- Add table to store chat history.
-- https://github.com/spring-projects/spring-ai/blob/cb967d10c9bf7daf03b648ffb210d315f2f1888b/memory/repository/spring-ai-model-chat-memory-repository-jdbc/src/main/resources/org/springframework/ai/chat/memory/repository/jdbc/schema-postgresql.sql#L2
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')),
    "timestamp" TIMESTAMP NOT NULL,

    CONSTRAINT fk_ai_conversation FOREIGN KEY (conversation_id)
    REFERENCES AI_CONVERSATION(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX
    ON SPRING_AI_CHAT_MEMORY(conversation_id, "timestamp");
--rollback DROP INDEX IF EXISTS SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX;
--rollback DROP TABLE IF EXISTS SPRING_AI_CHAT_MEMORY;