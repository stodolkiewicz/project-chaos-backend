# Google Cloud Storage: Pre-signed URLs Guide

### 1. Definition
A **Pre-signed URL** is a temporary, cryptographically signed link that provides restricted access to a private GCS object without requiring the requester to have Google credentials.

*   **Temporary Access:** Acts as a "guest pass" with a strictly defined expiration time.
*   **Scoped Permissions:** Grants access only for a specific action (e.g., `GET` for downloading).
*   **Security:** Files remain private; only holders of the valid signature can bypass access controls.

---

# Technical Process: GCS Pre-signed URL Generation (V4)

### 1. Construct the "String-To-Sign"
The Google Cloud SDK in your Java app creates a raw text template of the intended request.
*   **Components:** HTTP Verb (`GET`, `PUT`, etc.), Resource Path (`/bucket/file.pdf`), Expiration Timestamp, and Canonical Headers.
*   **Note:** The HTTP Verb here is what the **browser** will use to hit GCS, not your Spring controller's method.

### 2. Signing (Asymmetric Cryptography)
To ensure the template cannot be tampered with, it must be cryptographically signed.
*   **The "Pen" (Private Key):** Your backend sends the text to the **IAM Credentials API**. 
Google signs it using the **Service Account's Private Key** (which never leaves Google's secure environment).
*   **The Algorithm:** Typically **HMAC-SHA256**.
*   **The Result:** A unique string called the **Signature**.

### 3. URL Construction
The Backend assembles the final URL by appending the signature and metadata as query parameters:
`https://storage.googleapis.com/bucket/file.pdf?X-Goog-Signature=[HASH]&X-Goog-Algorithm=GOOG4-RSA-SHA256&...`

### 4. Verification by GCS
When the user’s browser hits the URL, Google Cloud Storage (GCS) performs a integrity check:
*   **Re-calculation:** GCS takes the parameters from the URL and recalculates the hash.
*   **The "Key" (Public Key):** GCS uses the **Service Account's Public Key** to verify that the signature was indeed created by the corresponding Private Key.
*   **Validation:**
    *   If `Calculated Hash == Signature in URL` -> **Access Granted**.
    *   If `Calculated Hash != Signature in URL` -> **403 Forbidden**.

---
### 1. Grant Storage Permissions (to Cloud Run SA)
`gcloud projects add-iam-policy-binding [PROJECT-ID] --member="serviceAccount:SA-ID" --role="roles/storage.objectAdmin"`
* **Purpose:** File management authorization.
* **Effect:** Enables the SA to upload, delete, and sign URLs in Cloud Storage.

### 2. Grant Impersonation Rights
`gcloud iam service-accounts add-iam-policy-binding SA-ID --member="user:youremailongcp@gmail.com" --role="roles/iam.serviceAccountTokenCreator"`
* **Purpose:** Secure local development.
* **Effect:** Allows your personal email to "borrow" the SA identity without JSON keys.

### 3. Enable IAM Credentials API
`gcloud services enable iamcredentials.googleapis.com`
* **Purpose:** Remote signing activation.
* **Effect:** Turns on the Google service required for dynamic token generation and URL signing.

### 4. Local Login with Impersonation
`gcloud auth application-default login --impersonate-service-account=SA-ID`
* **Purpose:** Local environment sync.
* **Effect:** Forces your local Spring Boot app to act as the Service Account.