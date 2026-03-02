# Design Document: Project Invitation System

## 1. Objective
Enable inviting users (both existing and new) to Kanban projects using their email addresses, integrated with Google OAuth authentication.

---

## 2. Comparison of Implementation Options

### Option 1: Shadow Profile (The "Ghost" User)
The system immediately creates a record in the `users` table upon invitation.

* **Version A (Primitive):** Created as a fully active user record.
* **Version B (Restricted):** Created with an `is_active = false` flag. This allows showing a **"Pending" badge** in the UI and running a **cleanup script** to delete user records that haven't logged in for 30 days.
* **Workflow:** Enter email → Create/Get User → Link to Project. Upon Google login, simply update the profile details (name, avatar).
* **Pros:** Extremely simple database logic; project access is ready instantly.
* **Cons:** Requires an "update" step during the first login to fill in profile details.

### Option 2: Invitation Table (The "Staging" Area)
The system stores invitations in a dedicated temporary table.

* **Workflow:** Enter email → Create record in `invitations` table. When a user logs in via Google, the system "sweeps" this table for their email, moves the access to `project_members`, and deletes the invitation.
* **Pros:** The `users` table remains clean, containing only people who have actually logged in.
Invitations can be displayed in the UI. 
Possibility for migration to Option 3.
* **Cons:** Requires extra logic during the login callback to check and migrate pending invites.

### Option 3: Active Invitation (Option 2 + Email)
This is **Option 2** but with an added automated email notification.

* **Workflow:** Stores the invite in the table **AND** triggers a notification (e.g., via SendGrid/GCP).
* **Pros:** Professional user experience; the invitee is actively prompted to join.
* **Cons:** Requires SMTP setup, email templates, and protection against bots/spam.

### Option 4: Token Link (The "Slack" Method)
The system generates a unique join link with a code (e.g., `app.com/join/xyz-123`).

* **Workflow:** Requires a **`tokens` table** to store the unique code, project ID, and role. Whoever clicks the link and logs in via Google gets project access.
* **Pros:** No need to know the invitee's email beforehand.
* **Cons:** Less secure (link can be forwarded); requires a dedicated table to manage tokens and their expiration.

---

## 3. Decision
Option 2 -> separation of concerns, possibility of displaying invitations in the UI and migration to Option 3