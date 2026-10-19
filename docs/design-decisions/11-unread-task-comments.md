# ADR 011: Unread Task Comments Indicator

## Status

Accepted

---

## Context

Users can comment on tasks. The comments are visible only in the task details dialog. A user has to open each task to find out whether someone else wrote something new.

The dashboard (`/dashboard`) shows tasks as cards. The frontend loads them with `GET /api/v1/projects/{projectId}/tasks?stage=...`. RTK Query refetches this list when the browser tab regains focus (`refetchOnFocus`). It also refetches the list after every task mutation.

### Constraints

- Each comment stores its creation time in `task_comments.created_date`
- The backend has no real-time channel to the browser (no WebSocket, no SSE for the dashboard)
- The backend runs on Cloud Run and may scale to multiple instances, so in-memory state is not an option
- A user may work on more than one device or browser

---

## Problem

Each task card on the dashboard must show how many comments of other users the current user has not seen yet. The user's own comments never count as unread.

---

## Decision

The backend stores when each user last opened the comments of each task. Comments of other users created after that moment are unread.

### Database

A new table `task_comment_reads` holds one row per user and task:

| Column | Type | Meaning |
|---|---|---|
| `user_id` | `UUID` | The reader. Foreign key to `users(id)` |
| `task_id` | `UUID` | The task. Foreign key to `tasks(id)` |
| `last_read_at` | `TIMESTAMP` | The moment the user last closed the comments of the task |

The primary key is `(user_id, task_id)`. Both foreign keys use `ON DELETE CASCADE`. Deleting a task or a user therefore removes its rows without extra code.

A missing row means the user has never opened the comments of the task. In that case all comments of other users count as unread.

### Backend

1. `PUT /api/v1/projects/{projectId}/tasks/{taskId}/comments/read` marks the comments of a task as read. It returns `204`. `TaskCommentsService.markTaskCommentsAsRead` loads the row with `findById`. When the row is missing, the method creates it. Then it sets `last_read_at` to `Instant.now()` and saves the row.
2. `GET /tasks` returns a new field `unreadCommentsCount` for every task. One grouped query counts unread comments for all returned tasks:
   ```sql
   SELECT tc.task.id, COUNT(tc)
   FROM TaskComments tc
   LEFT JOIN TaskCommentReadEntity r ON r.task.id = tc.task.id AND r.user.id = :userId
   WHERE tc.task.id IN :taskIds
   AND tc.author.id <> :userId
   AND (r.lastReadAt IS NULL OR tc.createdDate > r.lastReadAt)
   GROUP BY tc.task.id
   ```
   Replies are comments too, so they also count.
3. `TaskService.getTasks(projectId, stage, userId)` adds the counts to the task list. The AI tools keep using `getTasks(projectId, stage)`, which leaves `unreadCommentsCount` empty. The model does not need per-user read state.

### Frontend

1. A task card shows a `MessageSquare` icon with the number of unread comments when the number is greater than zero. Clicking the icon opens the task details dialog with the comments.
2. Closing the task details dialog calls `PUT .../comments/read`. The mutation invalidates the `BoardTasks`, `BacklogTasks` and `ArchivedTasks` tags. The task list is refetched and the icon disappears.
3. The dialog marks the comments as read on close, not on open. On open, the refetched list would return a zero count. The icon, which wraps the dialog, would unmount and close the dialog immediately.

---

## Failure Analysis

| Scenario | Result |
|---|---|
| Another user comments while the dashboard is open | The count updates on the next refetch: after the tab regains focus or after any task change. Accepted |
| Another user comments while the details dialog is open | The comment is marked as read on close, although the open dialog may not show it. Accepted |
| The `PUT .../read` request fails | The icon stays. The next close of the dialog retries the write |
| The user opens the same task on two devices | Both devices share the server-side state. The icon disappears on both after the next refetch |
| A comment is edited after it was read | Editing does not change `created_date`, so the comment stays read |
| A comment is deleted | The count drops on the next refetch |
| Two requests mark the same task as read at the same moment for a user who has no row yet | Both `findById` calls find nothing and both insert. The second insert violates the primary key and returns `500`. The first insert already stored a correct `last_read_at`, so no data is lost. Accepted |

---

## Alternatives Considered

### One row per read comment (`comment_reads(user_id, comment_id)`)

Each comment the user has seen gets its own row.

Rejected — the table grows with every comment and every reader. The dialog always shows all comments of a task at once, so per-comment precision gives no benefit over a single timestamp per task.

### Read state in `localStorage`

The browser stores the time the user last opened the comments of each task. The backend returns only the time of the newest comment by another user.

Rejected — the state does not follow the user to another browser or device. Clearing site data marks every comment as unread again.

### Atomic upsert with `INSERT ... ON CONFLICT`

PostgreSQL inserts the row or updates `last_read_at` of the existing row in one statement. Two simultaneous requests never fail.

Rejected — the statement needs a native query. HQL accepts `ON CONFLICT`, but Hibernate translates it to `MERGE` on PostgreSQL. `MERGE` does not protect against simultaneous inserts either, and it failed to bind the `Instant` parameter. A native query is not worth it for a race whose only effect is one failed request without data loss.

### Polling or push for live updates

The frontend polls `GET /tasks` every few seconds, or the backend pushes an event over WebSocket or SSE when a comment is created.

Rejected for now — the existing refetch on focus and after mutations is fast enough to notice new comments. Polling adds constant load. Push requires new infrastructure that works across Cloud Run instances. Revisit if users need to see comments the moment they are written.

---

## Consequences

**Pros:**
- Read state is shared across devices and browsers
- One small table with at most one row per user and task
- One extra query per `GET /tasks`, independent of the number of tasks
- No new infrastructure; the existing refetch strategy delivers the counts

**Cons:**
- New comments appear only after a refetch, not in real time
- `GET /tasks` needs the current user, so the response now differs per user

---

## Summary

`task_comment_reads` stores when each user last closed the comments of each task. `GET /tasks` returns `unreadCommentsCount` for every task, computed with one grouped query. The dashboard shows an icon with the count and marks comments as read when the user closes the task details dialog.
