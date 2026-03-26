# ADR 008: Attachment Upload Consistency Strategy

## Status

Accepted

---

## Context

The application allows users to upload file attachments to tasks. Files are stored in Google Cloud Storage (GCS), and metadata — including file path, size, owner, and vectorization status — is persisted in a PostgreSQL database via Spring Boot.

The upload flow involves two distinct external side effects:

1. **Storing a binary file in GCS**
2. **Persisting metadata in the database**

These two operations cannot be wrapped in a single atomic transaction. A failure between them leaves the system in an inconsistent state:

- File uploaded to GCS, but no DB record → orphaned file, storage leak
- DB record saved, but upload failed → broken reference, missing file

Additional responsibilities executed during upload:

- `increaseStorageUsage()` — tracks per-user and per-project byte consumption
- `FileTextExtractor.extractText()` — extracts raw text for vectorization

Both must remain consistent with the actual file state.

### Constraints

- Application runs on **Cloud Run** (ephemeral, scale-to-zero)
- No external message broker (no Pub/Sub, no RabbitMQ)
- Uploads handled via **multipart HTTP** (not presigned URLs at this stage)
- Vectorization is a background, non-critical operation

---

## Problem

The original implementation executed all steps sequentially within a single `@Transactional` method:

```java
@Transactional
public String uploadFile(...) {
    checkStorageLimits(...);
    storageService.uploadFile(file, filePath);   // GCS — not transactional
    increaseStorageUsage(...);
    attachmentRepository.save(attachment);        // DB
    return filePath;
}
```

**Failure scenarios:**

| Failure point | DB state | GCS state | Result |
|---|---|---|---|
| After `uploadFile`, before `save` | rolled back | file exists | Orphaned file in GCS |
| During `save` | rolled back | file exists | Orphaned file in GCS |
| After `save`, before commit | rolled back | file exists | Orphaned file in GCS |

In all cases the DB rolls back but GCS retains the file — causing storage leaks and incorrect usage accounting.

---

## Decision

Implement a **PENDING status + scheduled cleanup** strategy.

### Core Idea

Persist the attachment record with a `PENDING_UPLOAD` status **before** the GCS upload. After a successful upload, transition the record to `SAVED`. A scheduled cleanup job removes stale `PENDING_UPLOAD` records and their associated GCS files after a defined TTL (1 hour).

### Implementation

#### Upload method

```java
@Transactional
public String uploadFile(MultipartFile file, UUID projectId, UUID userId, UUID taskId) throws IOException {
    String originalFilename = file.getOriginalFilename();
    String sanitizedFilename = FileNameSanitizer.sanitize(originalFilename);
    String cloudStorageFilename = UUID.randomUUID() + "_" + sanitizedFilename;
    String filePath = String.format("projects/%s/tasks/%s/%s", projectId, taskId, cloudStorageFilename);

    checkStorageLimits(originalFilename, file.getSize(), projectId, userId);

    // 1. Persist record with PENDING status before touching GCS
    AttachmentEntity attachment = AttachmentEntity.builder()
            .id(UUID.randomUUID())
            .project(projectRepository.getReferenceById(projectId))
            .task(taskRepository.getReferenceById(taskId))
            .user(userRepository.getReferenceById(userId))
            .fileName(cloudStorageFilename)
            .originalName(originalFilename)
            .filePath(filePath)
            .contentType(file.getContentType())
            .fileSizeInBytes(file.getSize())
            .storageStatus(StorageStatusEnum.PENDING_UPLOAD)
            .vectorStatus(VectorStatusEnum.PENDING)
            .build();

    attachmentRepository.save(attachment);

    // 2. Upload to GCS — if this throws, DB rolls back, no orphan
    storageService.uploadFile(file, filePath);

    // 3. Mark as saved and increment usage
    increaseStorageUsage(projectId, userId, file.getSize());
    attachment.setStorageStatus(StorageStatusEnum.SAVED);
    attachmentRepository.save(attachment);

    // 4. Trigger text extraction asynchronously — non-critical
    applicationEventPublisher.publishEvent(new TextExtractionRequestedEvent(attachment.getId()));

    return filePath;
}
```

#### Async text extraction

```java
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleTextExtraction(TextExtractionRequestedEvent event) {
    try {
        byte[] bytes = storageService.downloadFile(event.filePath());
        String extractedText = FileTextExtractor.extractText(bytes);
        attachmentService.saveExtractedText(event.attachmentId(), extractedText);
    } catch (Exception e) {
        log.warn("Text extraction failed for attachment {}", event.attachmentId());
        attachmentService.markVectorizationFailed(event.attachmentId());
    }
}
```

#### Cleanup job

```java
@Scheduled(cron = "0 0 * * * *") // every hour
@Transactional
public void cleanupPendingAttachments() {
    Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

    List<AttachmentEntity> stale = attachmentRepository
        .findByStorageStatusAndCreatedAtBefore(StorageStatusEnum.PENDING_UPLOAD, oneHourAgo);

    stale.forEach(attachment -> {
        try {
            storageService.deleteFile(attachment.getFilePath());
        } catch (Exception e) {
            log.warn("Failed to delete orphaned GCS file: {}", attachment.getFilePath());
        }
        attachmentRepository.delete(attachment);
    });
}
```

---

## Failure Analysis

| Failure point | DB state | GCS state | Outcome |
|---|---|---|---|
| Before `uploadFile` (e.g. `checkStorageLimits`) | rolled back | no file | ✅ Clean |
| During `uploadFile` (GCS error) | rolled back | no file | ✅ Clean |
| After `uploadFile`, before `save(SAVED)` | PENDING remains | file exists | ⏳ Cleaned by job after 1h |
| Server crash between upload and second save | PENDING remains | file exists | ⏳ Cleaned by job after 1h |
| Text extraction fails | SAVED, vectorStatus=FAILED | file exists | ✅ Acceptable — retryable |

---

## Accepted Risks

### `increaseStorageUsage` may be skipped

If the server crashes after `uploadFile` but before `increaseStorageUsage`, storage counters will be under-reported until the cleanup job removes the PENDING record. This is considered acceptable — the window is short and the cleanup job self-corrects the accounting.

### Text extraction is best-effort

Extraction and vectorization happen asynchronously after commit. A failure here does not affect file availability. Records with `vectorStatus = FAILED` can be retried independently.

### Cleanup job runs in-process

The cleanup job uses `@Scheduled`, which is unreliable on Cloud Run (scale-to-zero, ephemeral instances). This is acceptable for cleanup — it is a best-effort operation, not a critical path. Missed runs simply delay orphan removal by one cycle. For production hardening, consider migrating to Cloud Scheduler → HTTP endpoint (see ADR 007).

---

## Alternatives Considered

### `TransactionSynchronizationManager.afterCompletion`

Delete the GCS file if the DB transaction rolls back. Rejected as the primary strategy — `deleteFile` itself can fail, leaving an orphan. Useful as an additional layer but not sufficient alone.

### `@TransactionalEventListener` + async upload (full async)

Upload to GCS after DB commit. Eliminates the consistency window but introduces latency before the file is available, requires the file bytes to survive across thread boundaries, and complicates the presigned URL return flow. Overhead not justified at current scale.

### Transactional Outbox Pattern

Persist an outbox event in the same DB transaction as the attachment record. A separate processor picks up events and performs the GCS upload with retry guarantees. Most robust solution, but requires storing file bytes outside the request scope (temp disk or staging bucket) and adds significant operational complexity. Revisit if at-least-once delivery guarantees become a hard requirement.

### Direct upload via presigned URL

Issue a presigned GCS upload URL to the client; the browser uploads directly to GCS. Eliminates server-side streaming and resolves the consistency problem cleanly. Deferred — requires frontend changes and a `confirmUpload` step. Recommended for a future iteration.

---

## Consequences

**Pros:**
- Simple to implement and reason about
- No additional infrastructure required
- Handles the common failure case (GCS error rolls back DB cleanly)
- Vectorization failures are isolated and retryable

**Cons:**
- PENDING records visible in DB during upload window
- Cleanup job may not run reliably on Cloud Run (best-effort)
- `increaseStorageUsage` can be transiently inconsistent
- Does not guarantee at-least-once delivery for text extraction

---

## Summary

The chosen strategy is a pragmatic balance between consistency and complexity. It eliminates the most common failure mode (orphaned GCS files with no DB record) by recording intent before acting, and delegates edge-case cleanup to a background job. Non-critical operations (text extraction, vectorization) are decoupled via `@TransactionalEventListener` and treated as best-effort.