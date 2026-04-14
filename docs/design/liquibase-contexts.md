# Database Migrations & Environment Contexts

This project employs **Liquibase contexts** to manage schema evolution and data population.   
This ensures that development-specific test data never pollutes the production environment.

## Environment Configuration

Contexts are activated via Spring profiles in the `application-*.yml` files:

* **Development (`dev`)**:
    * `liquibase.contexts: dev`
    * `drop-first: true` (Ensures a clean database state on every application start).
* **Production (`prod`)**:
    * `liquibase.contexts: prod`
    * Strictly for structural updates

## Migration Strategy (DDL vs DML)

### 1. DDL (Data Definition Language)
Structural changes such as creating tables, adding columns, or defining indexes must be applied to **all** environments.  
Tag these with both contexts.
```sql
--changeset andrzej:101 context:dev,prod
--description: Create chat_log table for AI usage tracking
CREATE TABLE IF NOT EXISTS chat_log (
                                        id UUID PRIMARY KEY,
                                        user_id VARCHAR(255) NOT NULL,
    conversation_id VARCHAR(255),
    prompt_tokens INT NOT NULL,
    completion_tokens INT NOT NULL,
    total_tokens INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_chat_log_user_created ON chat_log(user_id, created_at);

--rollback DROP INDEX IF EXISTS idx_chat_log_user_created;
--rollback DROP TABLE IF EXISTS chat_log;
```

### 2. DML (Test & Mock Data)
Restricted to the **dev** context.

```sql
--changeset andrzej:102 context:dev
--description: Add dummy tasks for local development
INSERT INTO tasks (title, status) VALUES ('Refactor AI Service', 'IN_PROGRESS');
--rollback DELETE FROM tasks WHERE title = 'Refactor AI Service';
```