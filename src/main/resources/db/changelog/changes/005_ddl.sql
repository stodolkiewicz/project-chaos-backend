--liquibase formatted sql

--changeset stodo:20 context:dev,prod
ALTER TABLE users ALTER COLUMN first_name DROP NOT NULL;
ALTER TABLE users ALTER COLUMN last_name DROP NOT NULL;
--rollback ALTER TABLE users ALTER COLUMN first_name SET NOT NULL;
--rollback ALTER TABLE users ALTER COLUMN last_name SET NOT NULL;