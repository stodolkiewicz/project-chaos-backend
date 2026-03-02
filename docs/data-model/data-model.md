## Data Model
![db_schema.png](../images/db_schema.png)

# Database Schema Documentation

### 1. Users & Permissions
* **`users`**: Central profile table. Stores Google OAuth data (`email`, `first_name`, `last_name`, `google_picture_link`) and Spring Security flags. Includes `default_project_id`.
* **`project_users`**: Many-to-Many join table. Defines project membership and the user's specific `project_role`.
* **`invitations`**: Email-based "staging area" for invites. Records are converted to `project_users` entries upon the user's first login.

### 2. Project Structure
* **`projects`**: Main data container. Stores name, description, and audit metadata (who/when created and modified).
* **`columns`**: Defines workflow stages (lists) within a project. The `position` field determines the horizontal order on the board.
* **`labels`**: Project-specific dictionary of tags. Each label consists of a name and a color code.

### 3. Task Management
* **`tasks`**: The core entity. Stores task content, description, and `position_in_column`. Linked to a priority and an `assignee_id`.
* **`task_priorities`**: Project-specific priority levels. Defines `priority_value` (weight) and colors (e.g., "High", "Low").
* **`task_labels`**: Many-to-Many join table. Enables assigning multiple labels to a single task.

### 4. Communication & Assets
* **`task_comments`**: Stores discussion history for each task, including author and timestamp.
* **`attachments`**: Metadata for files attached to tasks (file name, storage URL, and uploader info).

---

### Technical Standards
* **PK/FK**: All identifiers follow the `UUID` standard for uniqueness and security.
* **Audit Logs**: `created_date`, `last_modified_date`, and `last_modified_by` fields provide full change tracking.
* **Optimistic Locking**: The `version` field prevents data overwrites during concurrent edits.