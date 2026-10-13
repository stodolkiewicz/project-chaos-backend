--liquibase formatted sql

--changeset stodo:41 context:dev,prod
-- Spring AI 2.0 orders and writes chat memory messages using the sequence_id column.
ALTER TABLE SPRING_AI_CHAT_MEMORY ADD COLUMN IF NOT EXISTS sequence_id BIGINT;

UPDATE SPRING_AI_CHAT_MEMORY m
SET sequence_id = o.rn
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY conversation_id ORDER BY "timestamp", id) - 1 AS rn
    FROM SPRING_AI_CHAT_MEMORY
) o
WHERE m.id = o.id;

ALTER TABLE SPRING_AI_CHAT_MEMORY ALTER COLUMN sequence_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_SEQUENCE_ID_IDX
    ON SPRING_AI_CHAT_MEMORY(conversation_id, sequence_id);
--rollback DROP INDEX IF EXISTS SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_SEQUENCE_ID_IDX;
--rollback ALTER TABLE SPRING_AI_CHAT_MEMORY DROP COLUMN IF EXISTS sequence_id;
