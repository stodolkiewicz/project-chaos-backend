# TODO: task-updated Kafka event + transactional outbox

## Goal
Publish a `task-updated` event to Kafka whenever a task is created/updated/deleted.
A separate consumer (worker) will pick it up and update OpenSearch/Elasticsearch
(so users can filter tasks by stage, priority, labels, assignee, etc.).

## Current state (already done)
- Avro schema `TaskUpdatedMessage` created in repo `project-chaos-avro-schemas`
  (`src/main/avro/TaskUpdatedEvent.avsc`), tagged `2.0.0`, published via JitPack
  (`com.github.stodolkiewicz:project-chaos-avro-schemas:2.0.0`). Backend resolves it.
- Generated Avro classes available: `TaskUpdatedMessage`, `TaskLabel`, `EventType` (UPSERT/DELETE),
  namespace `com.stodo.projectchaos.kafka`.
- `KafkaConfig` updated: single `ProducerFactory<Void, SpecificRecord>` +
  `KafkaTemplate<Void, SpecificRecord>` (KafkaAvroSerializer is type-agnostic, one template
  serves all Avro types). Topic bean `createTaskUpdatedTopic` added (3 partitions, 1 replica, dev).
- `app.kafka.topic.task-updated` property wired.
- `KafkaTaskUpdatedService` skeleton exists at
  `src/main/java/com/stodo/projectchaos/kafka/taskupdated/KafkaTaskUpdatedService.java`
  (currently `sendTaskUpdatedMessage(TaskUpdatedMessage)` returning null — needs rework, see below).

## Avro schema fields (TaskUpdatedMessage)
taskId (uuid), projectId (uuid), eventType (enum UPSERT/DELETE),
title?, description?, stage?, assigneeId? (uuid), priorityId? (uuid), priorityName?,
labels (array of TaskLabel{labelId uuid, labelName}), createdDate (timestamp-millis),
lastModifiedDate (timestamp-millis).
- UPSERT covers both create & update (consumer = delete+insert anyway).
- DELETE only needs taskId + projectId + eventType, rest null.

## Key design decisions (agreed)
1. **Event carries IDs, not just names**: keep `priorityId` and `labelId` in the event so that a
   future "rename priority/label" admin operation can do a single bulk update in OpenSearch by id
   (instead of flooding Kafka with one event per affected task — that would kill throughput).
2. **assigneeId** is enough (stable UUID, never changes). Reassigning assignee = task change = event
   fires anyway. Email NOT needed (already migrated assignee_email -> assignee_id in migration 006).
3. **Priority/label names are currently never updated** in the app (TaskPriorityService/LabelService
   are read-only, no update endpoint). Label name is treated as immutable key (only color can change,
   see CustomTaskRepository.saveOrUpdateLabel). So for now the common flow needs no special handling.
4. Avro `TaskUpdatedMessage` must NOT leak into the business layer. It lives ONLY inside
   `KafkaTaskUpdatedService` (the wire boundary).

## Architecture to implement (mirror the existing Vectorization outbox flow)

Reference implementation already in repo (copy its shape):
- `kafka/vectorization/VectorizationEventListener.java` (@TransactionalEventListener AFTER_COMMIT)
- `kafka/vectorization/VectorizationRequestedEvent.java` (plain POJO, carries ids)
- `kafka/vectorization/KafkaVectorizationService.java` (builds Avro, sends, deletes outbox row)
- `features/vectorizationoutbox/VectorizationOutboxService.java` (save/findPending/markAsFailed/delete)
- `kafka/BackupVectorizationScheduler.java` (retry pending rows)
- entity `model/entity/VectorizationOutboxEntity.java`, enum `VectorizationOutboxStatusEnum`
- migrations 021/022 for the outbox table

### CRITICAL DIFFERENCE from vectorization outbox
Vectorization outbox stores only a **reference** (attachment FK) and re-fetches data at send time.
For task-updated we have **DELETE** events → you CANNOT re-fetch a deleted task.
=> The task-updated outbox row MUST STORE THE FULL PAYLOAD (title, stage, priorityName, labels, ...),
   captured at event time.

### Layering (avoids the "record same as Avro message" duplication)
Do NOT create a separate standalone domain record duplicating the Avro message.
The outbox domain object IS your domain representation (needed anyway for the retry scheduler):

```
TaskUpdatedOutboxEntity   (DB - columns = payload)
        | MapStruct
TaskUpdatedOutbox         (domain object - this is the "record")
        | MapStruct / Avro builder
TaskUpdatedMessage        (Avro - only inside KafkaTaskUpdatedService)
```

### Components to build
- **TaskService** (in its @Transactional create/update/delete methods):
  build the data, call `taskUpdatedOutboxService.save(...)`, publish a Spring event carrying outboxId.
- **TaskUpdatedOutboxService**: persist payload into the outbox entity (same tx). Plus
  findPending / markAsFailed / deleteById like VectorizationOutboxService.
- **TaskUpdatedEventListener** `@TransactionalEventListener(AFTER_COMMIT)`:
  calls `kafkaTaskUpdatedService.send(outbox)`.
- **KafkaTaskUpdatedService**: map domain outbox -> Avro `TaskUpdatedMessage`, send to Kafka,
  delete outbox row on success / leave on failure. (Rework current skeleton: take the domain
  outbox object, NOT the raw Avro message. Two entry points make sense: send for UPSERT and
  for DELETE, each sets EventType itself.)
- **Outbox entity + Liquibase migration** (new table, follow naming `XXX_ddl_...sql`, context dev,prod;
  remember Auditable columns if it extends Auditable: created_date, last_modified_date,
  last_modified_by, version). Use hardcoded UUIDs if any test data.
- **Backup scheduler** for retries (mirror BackupVectorizationScheduler), respecting a max-retry
  property like the existing `app.kafka.attachment-vectorization-max-retry-count`.

## Misc notes
- ProjectBacklogEntity is effectively dead/duplicate: task placement in backlog is really driven by
  `TaskEntity.stage` (TaskStageEnum: BACKLOG/BOARD/ARCHIVED, default BOARD). ProjectBacklog is a
  half-implemented leftover (see TODO in CustomTaskRepository:88). Not part of this task, just context.
- JitPack gotcha (in case of version bumps): publish requires a git tag matching the version
  (e.g. tag `2.0.0`). First request only triggers an async build; JitPack may cache a `tagNotFound`
  if queried before the tag existed. If backend can't resolve: confirm tag on remote
  (`git ls-remote --tags origin`), then `mvn clean install -U`; if still failing remove negative
  cache: `rm -rf ~/.m2/repository/com/github/stodolkiewicz/project-chaos-avro-schemas`. After Maven
  resolves, IntelliJ needs an explicit "Reload All Maven Projects" (reopen is not enough).
