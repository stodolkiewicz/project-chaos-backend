# ADR 007: Storage Cost Control for Attachments (GCS)

## Status

Accepted

---

## Context

The application allows users to upload attachments to tasks. Files are stored in Google Cloud Storage.

Constraints and challenges:

* No native bucket size limits in GCS
* Storage growth directly increases cost
* Multi-tenant model based on **users and projects** (no workspaces)
* Application runs on Cloud Run (ephemeral, autoscaling)
* Uploads are handled via signed URLs

We need to:

* enforce fair usage per user and per project
* prevent large file abuse
* protect against global cost spikes
* remain compatible with Cloud Run (scale-to-zero)

---

## Decision

Implement a **multi-layered storage control strategy** with three enforcement levels:

---

### 1. File-Level Control (Hard Limit)

Each upload must respect a maximum file size.

#### Implementation:

* Validate requested file size before generating signed URL

#### Example:

* `MAX_FILE_SIZE = 20MB`

#### Flow:

1. Client requests upload
2. Backend validates file size
3. Reject if exceeded

#### Purpose:

* Prevent extreme single uploads
* Protect system from abuse and accidental large files

---

### 2. Application-Level Limits (Primary Control)

Track storage usage per:

* **user**
* **project**

#### Data Model:

**attachments**

* `id`
* `project_id`
* `user_id`
* `file_size_in_bytes`
* `file_path`
* `original_name`
* `content_type`
* `extracted_text`
* `vector_status` Enum PENDING, PROCESSED, FAILED
* `created_at`

**user_storage_usage**

* `user_id`
* `used_bytes`
* `limit_bytes`

**project_storage_usage**

* `project_id`
* `used_bytes`
* `limit_bytes`

---

#### Flow:

**Before upload (signed URL generation):**

1. Validate:

   * `user.used_bytes + file_size <= user.limit`
   * `project.used_bytes + file_size <= project.limit`
2. Reject if any limit exceeded

**After upload:**

1. Persist attachment metadata
2. Increment both:

   * user usage
   * project usage

---

#### Notes:

* This is the **primary enforcement mechanism**
* Must be consistent and indexed
* Prevents abuse at the business logic level

---

### 3. Global Kill Switch (Fail-Safe)

Protect against unexpected cost spikes or logic failures.

#### Implementation:

* Scheduled job checks total bucket size
* If threshold exceeded → block all uploads

---

#### Flow:

**Endpoint:**

```
GET /internal/check-storage
```

**Logic:**

1. Fetch total bucket size (Monitoring API)
2. Compare with global limit (e.g. 50 GB)
3. Store result in DB:

   * `uploads_blocked = true/false`

---

**Upload flow:**

1. Check global flag
2. If `true` → reject all uploads

---

#### Purpose:

* Final safety net
* Prevents runaway costs even if app-level limits fail

---

### 4. Scheduling Strategy (Cloud Run Compatible)

Avoid in-process schedulers due to:

* instance lifecycle unpredictability
* scale-to-zero behavior
* multiple execution risk

---

#### Decision:

Use **Cloud Scheduler → HTTP endpoint**

---

#### Flow:

1. Cloud Scheduler triggers endpoint every 5 minutes
2. Endpoint runs in existing Spring backend
3. Updates global flag

---

#### Benefits:

* Works with scale-to-zero
* No always-on instances
* Predictable execution

---

### 5. Secure Invocation (OIDC / IAM)

Avoid public endpoints and static secrets.

---

#### Implementation:

* Cloud Scheduler uses a dedicated Service Account
* Sends OIDC token (JWT)
* Backend validates token via Spring Security

---

#### Guarantees:

* Only authorized scheduler can trigger job
* No secrets in code
* Fully IAM-based security model

---

### 6. Lifecycle Rules (Optional Safety Net)

Configure Google Cloud Storage lifecycle:

* delete orphaned files
* clean old data if needed

---

#### Purpose:

* Not primary control
* Helps reduce long-term storage costs

---

## Consequences

### Pros:

* **Strong cost control** at multiple levels
* **Cloud Run compatible** (no reliance on in-memory jobs)
* **Fair usage enforcement** (user + project)
* **Secure scheduling** (OIDC, no secrets)
* **Simple infrastructure** (no Redis, no extra services)

---

### Cons:

* **Eventual consistency** in bucket size checks
* **Race conditions** possible during concurrent uploads
* **Additional DB overhead**
* Slightly more complex logic

---

## Alternatives

### In-process Scheduler (`@Scheduled`)

Rejected:

* unreliable in Cloud Run
* breaks with scaling
* not deterministic

---

## Summary

The system enforces storage limits using:

1. **File size limit** → prevents large uploads
2. **User + project limits (DB)** → primary control
3. **Global kill switch (bucket size)** → fail-safe
4. **Cloud Scheduler + HTTP** → reliable execution
5. **OIDC security** → production-grade access control

This ensures **predictable costs, abuse prevention, and Cloud Run compatibility** without adding unnecessary infrastructure.
