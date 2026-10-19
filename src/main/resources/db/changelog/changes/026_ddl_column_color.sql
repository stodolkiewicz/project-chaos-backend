--liquibase formatted sql

--changeset stodo:44 context:dev,prod
-- Each board column has a color key from a fixed palette (ColumnColorEnum).
-- Existing columns get palette colors in the order of their position in the project.
ALTER TABLE columns ADD COLUMN color VARCHAR(10);

UPDATE columns c
SET color = (ARRAY['GRAY', 'BLUE', 'GREEN', 'AMBER', 'RED', 'PURPLE', 'PINK', 'TEAL'])[((ordered.row_number - 1) % 8) + 1]
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY project_id ORDER BY position) AS row_number
    FROM columns
) ordered
WHERE c.id = ordered.id;

ALTER TABLE columns ALTER COLUMN color SET NOT NULL;
--rollback ALTER TABLE columns DROP COLUMN color;
