--liquibase formatted sql

--changeset stodo:25 context:dev,prod
CREATE TABLE IF NOT EXISTS AI_CONVERSATION (
    id VARCHAR(36) PRIMARY KEY, --because default SPRING_AI_CHAT_MEMORY conversation_id is VARCHAR(36)
    project_id UUID not null,
    user_id UUID not null,
    conversation_has_title BOOLEAN DEFAULT FALSE,
    title VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_project_id FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);
--rollback DROP TABLE IF EXISTS AI_CONVERSATION;
