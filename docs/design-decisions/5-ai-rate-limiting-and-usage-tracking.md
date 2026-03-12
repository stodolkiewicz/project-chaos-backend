# ADR 004: Persistent Rate Limiting & AI Usage Tracking

## Status
Accepted

## Context
The application runs on **GCP Cloud Run** (ephemeral/serverless). In-memory caching is unreliable due to frequent instance restarts and horizontal scaling. We need to prevent request storms and track costs without adding infrastructure complexity.

## Decision
Implement a **SQL-based Rolling Window (1h)** for rate limiting and auditing.

### Implementation:
* **Storage**: PostgreSQL (`chat_log` table).
* **Window**: 1 hour.
* **Logic**:
    1. **Pre-request**: `SELECT COUNT(*) FROM chat_log WHERE user_id = ? AND created_at > (NOW() - INTERVAL '1 hour')`.
    2. **Threshold**: Reject if count > 20.
    3. **Post-request**: Async insert of `prompt_tokens`, `completion_tokens`, and `total_tokens`.

## Consequences
* **Pros**:
    * **Cloud Run Compatible**: Limits persist across instance restarts and scaling.
    * **Audit Ready**: Permanent record of costs and user behavior.
    * **Simplicity**: No new infrastructure; uses existing DB.
* **Cons**:
    * **DB Overhead**: Extra I/O per request (mitigated by indexing `user_id` and `created_at`).

## Alternatives
* **Redis (Marketplace Free Tier)**: **Postponed**. Superior performance, but Postgres is sufficient for current scale and avoids extra config.
* **Caffeine (RAM)**: **Rejected**. Ephemeral nature of Cloud Run makes it useless for persistent limiting.