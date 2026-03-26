--liquibase formatted sql

--changeset stodo:36 context:dev,prod
-- Populate user_storage_usage table for existing users with 300MB limit (314572800 bytes)
INSERT INTO user_storage_usage (id, user_id, used_bytes, limit_bytes, created_date, last_modified_date, last_modified_by, version)
SELECT 
    gen_random_uuid() as id,
    id as user_id,
    0 as used_bytes,
    314572800 as limit_bytes, -- 300MB in bytes
    NOW() as created_date,
    NOW() as last_modified_date,
    'system' as last_modified_by,
    0 as version
FROM users;

--changeset stodo:37 context:dev,prod
-- Populate project_storage_usage table for existing projects with 600MB limit (629145600 bytes)
INSERT INTO project_storage_usage (id, project_id, used_bytes, limit_bytes, created_date, last_modified_date, last_modified_by, version)
SELECT 
    gen_random_uuid() as id,
    id as project_id,
    0 as used_bytes,
    629145600 as limit_bytes, -- 600MB in bytes
    NOW() as created_date,
    NOW() as last_modified_date,
    'system' as last_modified_by,
    0 as version
FROM projects;

--rollback DELETE FROM project_storage_usage;
--rollback DELETE FROM user_storage_usage;