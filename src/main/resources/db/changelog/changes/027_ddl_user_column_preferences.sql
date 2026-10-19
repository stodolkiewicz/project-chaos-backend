--liquibase formatted sql

--changeset stodo:45 context:dev,prod
-- Stores what a user hears and sees after dropping a task into a column.
-- A column without a row uses the default: the last column plays a random sound with confetti, others stay silent.
CREATE TABLE IF NOT EXISTS user_column_preferences (
    user_id UUID NOT NULL,
    column_id UUID NOT NULL,
    sound VARCHAR(10) NOT NULL,
    confetti BOOLEAN NOT NULL,

    PRIMARY KEY (user_id, column_id),
    CONSTRAINT fk_user_column_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_column_preferences_column FOREIGN KEY (column_id) REFERENCES columns(id) ON DELETE CASCADE
);
--rollback DROP TABLE IF EXISTS user_column_preferences;
