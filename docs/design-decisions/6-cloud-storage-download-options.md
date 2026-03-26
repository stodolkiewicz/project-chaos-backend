# ADR 006: File Download Strategy for Kanban Attachments

## Status
Accepted

## Context
The Kanban application allows users to upload and download task attachments. The backend runs on **GCP Cloud Run** (serverless), and files are stored in **Google Cloud Storage (GCS)**. We need a secure, cost-effective, and scalable way to serve these files to the Next.js frontend without degrading backend performance or exposing private data.

## Decision
Implement **GCS Pre-signed URLs (V4)** for all private file downloads.

### Implementation:
* **Storage**: Google Cloud Storage (Private Bucket).
* **Security**: Backend generates a time-bound (e.g., 15 minutes) HMAC-SHA256 signature using the IAM Credentials API and a dedicated Service Account.
* **Flow**:
    1. Next.js requests access to a file.
    2. Spring Boot verifies user permissions and returns a Signed URL.
    3. Next.js fetches the file directly from GCS using the provided URL.

## Consequences
* **Pros**:
    * **Zero Server Overhead**: Cloud Run memory and CPU are completely bypassed during data transfer.
    * **Cost-Efficient**: Eliminates double-egress network costs (GCS -> Backend -> User).
    * **Secure**: Files remain strictly private; access is time-limited and cryptographically verified.
* **Cons**:
    * **Complexity**: Requires IAM configuration (Service Account impersonation) and an extra API call to get the URL before the actual download.

## Alternatives

* **Alternative 1: Backend Download (Proxy via Spring Boot)**
    * **Status**: **Rejected**.
    * **Reason**: Routing file bytes through the backend consumes Cloud Run RAM and CPU. It severely limits horizontal scalability, increases latency, and doubles egress costs.

* **Alternative 2: Public URL (Unauthenticated GCS Link)**
    * **Status**: **Rejected**.
    * **Reason**: Setting the GCS bucket or objects to public (`allUsers`) would expose sensitive project/Kanban attachments to the internet. Unacceptable for data privacy.