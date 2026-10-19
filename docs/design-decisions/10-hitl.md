# ADR 010: Human-in-the-Loop for LLM Task Creation and Deletion

## Status

Accepted

---

## Context

The application has an LLM chat built with **Spring AI** (`ChatClient`). The model can call tools to work with tasks. `getTasks` already exists and reads tasks for the current user. Two new tools are being added: `createTask` and `deleteTask`.

The chat response is **streamed** to the frontend as Server-Sent Events (SSE). The controller method `chat` handling `POST /chat` returns `Flux<ServerSentEvent<Object>>`, built from `chatClient.prompt()...stream().content()`.

By default, Spring AI executes every tool the model requests immediately. For `createTask` and `deleteTask` this is not acceptable: a task must only be created or deleted after the user explicitly confirms it.

### Constraints

- The response is streamed over SSE; the solution must work during an open stream
- During streaming, Spring AI may execute tools on a different thread than the controller, so `SecurityContextHolder` cannot be relied on inside tools
- The backend runs on Cloud Run and may scale to multiple instances; in-memory state shared between requests is not an option
- Endpoints for creating and deleting tasks already exist and are used by the regular UI (`POST /tasks`, `DELETE /tasks/{id}`)
- Creating and deleting tasks are not critical operations; no audit trail of user decisions is required
- Project is in early development; operational overhead should be minimal

---

## Problem

The model must be able to propose creating or deleting a task, but the actual change must be performed only after user confirmation. In addition:

1. The proposal must reach the frontend while the response is still streaming
2. The model does not learn whether the user accepted or rejected the proposal, so it must not assume either outcome later in the conversation

---

## Decision

The tools `createTask` and `deleteTask` **do not modify tasks**. Instead, they emit an additional SSE event into the stream. The frontend reacts to the event with a confirmation UI, and the user performs the action through the existing task endpoints. Nothing is persisted on the backend for a pending proposal.

### Backend

1. The `chat` method in the controller handling `POST /chat` creates a `Sinks.Many<ServerSentEvent<Object>>` per request:
   ```java
   Sinks.Many<ServerSentEvent<Object>> sink = Sinks.many().unicast().onBackpressureBuffer();
   ```
   and passes it to the tools via `ToolContext` under the key `"events"`.
2. `createTask` emits an event named `RequestCreateTask` with the proposed data (e.g. `{"title": "Zakupy"}`).
3. `deleteTask` emits an event named `RequestDeleteTask` with the task data (e.g. `{"taskId": 42, "title": "Zakupy"}`).
4. The `chat` method returns `Flux.merge(tokenEvents, sink.asFlux())`, where `tokenEvents` are the streamed text chunks mapped to events named `token`. Tool events reach the frontend as soon as the tool runs, before the model finishes writing.
5. When the text stream completes, `doOnComplete(sink::tryEmitComplete)` completes the sink. Without it, `Flux.merge` never completes and the SSE connection stays open.
6. Tools emit with `sink.emitNext(event, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(100)))` instead of `tryEmitNext`, so that an event is not lost if two tools emit at the same time (`FAIL_NON_SERIALIZED`).

### Frontend

1. The stream is consumed with `fetchEventSource` (`@microsoft/fetch-event-source`), because the native `EventSource` supports only `GET`.
2. `RequestCreateTask` opens a task creation form prefilled with the data from the model. The user can edit it before saving. Saving calls `POST /tasks`.
3. `RequestDeleteTask` opens a confirmation popup. Confirming calls `DELETE /tasks/{id}`.
4. Cancelling closes the form or popup without any backend call.

### Model knowledge of the outcome

The model never receives the user's decision. To prevent it from assuming an outcome:

1. The tool description (`@Tool(description = ...)`) states that the tool only shows a proposal to the user and does not change any task.
2. The text returned by the tool states that the proposal was shown and it is unknown whether the user will accept it, e.g. *"The user was shown a confirmation popup for deleting task 42. It is unknown whether they will confirm it."*
3. The system prompt instructs the model to check the current state with `getTasks` at the first opportunity after proposing a task creation or deletion, and whenever it needs to know whether a proposed action was performed.

The task table remains the single source of truth: the model learns the outcome by reading it, not from a stored decision.

---

## Failure Analysis

| Scenario | Result |
|---|---|
| User reloads the page before deciding | Event is lost, task is unchanged. User has to ask the model again. Accepted |
| User never decides | Nothing happens; no state to clean up |
| User edits the title in the creation form | Created task differs from the proposal; model sees the real title via `getTasks` |
| User deletes the task with the regular UI instead of the popup | Model sees the real state via `getTasks` |
| Model claims the task was created or deleted | Mitigated by the tool description, tool return text and system prompt. Residual risk accepted |
| Model does not follow the instruction to call `getTasks` | Model may answer from stale conversation memory. Residual risk accepted |
| User types "yes, delete it" instead of clicking | Model may call `deleteTask` again, showing a second popup. Accepted |
| Two tools emit into the sink at the same time | `emitNext` with `busyLooping` retries, the event is not lost |
| Sink is not completed | `Flux.merge` never completes and the SSE connection stays open. Prevented by `doOnComplete(sink::tryEmitComplete)` |
| Request handled by a different Cloud Run instance than the confirmation | No impact; the confirmation calls the regular task endpoints and needs no backend state |

---

## Accepted Risks

### Proposals are lost on page reload

Proposals exist only in the open SSE stream and in the frontend. Reloading the page before deciding loses them. Accepted — creating and deleting tasks are not critical operations, and the user can simply ask again.

### No record of user decisions

There is no audit trail of who accepted or rejected a proposal. Accepted — not required for task management. The resulting state of tasks is visible in the task table.

### Model may not verify the state

The instruction to call `getTasks` is a prompt, not an enforced mechanism. Accepted — the consequence is an inaccurate chat answer, not an incorrect change to data.

---

## Alternatives Considered

### Persisting proposals in a `pending_action` table

Same SSE flow, with one addition: before emitting the event, `createTask` and `deleteTask` insert a row into a new `pending_action` table (`id`, `user_id`, `conversation_id`, `type`, `task_id`, `task_title`, `status` = `WAITING` / `ACCEPTED` / `REJECTED`, `created_at`). The event carries the row `id` as `actionId`. The frontend confirms with `POST /pending-actions/{actionId}/accept` or `/reject` instead of calling the task endpoints directly. The accept endpoint changes the status with a single conditional `UPDATE ... WHERE id = ? AND user_id = ? AND status = 'WAITING'` (protecting against double clicks) and performs the task change in the same transaction. A `GET /conversations/{conversationId}/pending-actions` endpoint lets the frontend restore open proposals after a page reload.

Rejected — creating and deleting tasks are not critical operations. An audit trail of who accepted or rejected a proposal is not needed, and losing a proposal on page reload is not a significant problem. The table, the migration, three new endpoints and duplicate-proposal handling would add complexity without a meaningful benefit. Revisit if tools that perform critical or irreversible operations are added.

### Writing the user's decision to chat memory

When the user confirms, the backend appends a note such as *"The user confirmed deleting task 42"* to the conversation memory, and the system prompt states that a missing confirmation means the action did not happen.

Rejected — the task endpoints do not know the `conversationId`, so a separate endpoint would be needed. The note could be stored before the model's answer that it refers to, because the popup appears while the response is still streaming. Some model providers do not accept consecutive messages of the same role or a system message in the middle of a conversation. The rule "no confirmation means no action" is wrong when the note fails to save or the task is changed through the regular UI.

### User-controlled tool execution (`internalToolExecutionEnabled(false)`)

Spring AI does not execute tools automatically; the application runs the tool-calling loop itself, stores the whole conversation when a tool requiring approval is requested, and resumes it after confirmation.

Rejected — with streaming, the application would have to detect tool calls within the stream and start new streams in a loop, and the conversation state would have to be serialized and stored between requests. The most complex option for no benefit in this use case.

---

## Consequences

**Pros:**
- The model can never create or delete a task by itself; only a user action through the existing endpoints changes data
- No new tables, migrations or endpoints on the backend
- Stateless; works with multiple Cloud Run instances
- Existing authorization of `POST /tasks` and `DELETE /tasks/{id}` applies unchanged
- The creation form lets the user correct the model's proposal before saving
- The task table is the single source of truth for the model

**Cons:**
- Proposals are lost on page reload
- No audit trail of user decisions
- The model learns the outcome only if it follows the instruction to call `getTasks`

---

## Summary

The `createTask` and `deleteTask` tools do not modify tasks. They emit `RequestCreateTask` and `RequestDeleteTask` SSE events through a per-request `Sinks.Many` merged into the chat stream. The frontend shows a form or a confirmation popup, and the user performs the action through the existing task endpoints. Nothing is persisted for pending proposals. The model is told that the outcome is unknown and is instructed to check the current state with `getTasks` at the first opportunity.
