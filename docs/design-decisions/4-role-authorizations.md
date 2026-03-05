# Design Decision: Project Ownership & Role Hierarchy

## Context
Current RBAC (Role-Based Access Control) implementation lacks a root authority, leading to administrative deadlocks.

## Current State (As-Is)
* **Roles:** `ADMIN`, `MEMBER`, `VIEWER`.
* **Authority:** `ADMIN` manages `MEMBER` and `VIEWER`.
* **Constraint:** `ADMIN` cannot modify or remove other `ADMIN`s.

## Problem: Administrative Deadlock
* **Irrevocability:** If all members are promoted to `ADMIN`, the project state becomes frozen (no one can be demoted or removed).
* **Exit Strategy:** The "Last Admin" is blocked from leaving to prevent orphaned projects, but no formal transfer mechanism exists.

## Solution: Introduction of OWNER Role
Establish a single, immutable root of trust per project.

| Role | Authority | Constraints |
| :--- | :--- | :--- |
| **OWNER** | Full control (can demote/remove `ADMIN`s). | Exactly 1 per project. |
| **ADMIN** | Manages `MEMBER`/`VIEWER`. | Cannot modify `OWNER` or peers. |



## Implementation Details
1. **Database Schema:** Add `owner_id` column to `projects` table (FK to `users`).
2. **Transfer Logic:** `OWNER` must transfer ownership to another member before leaving or deleting the project.
3. **Admin Stability:** `ADMIN`s can exit the project freely as the `OWNER` serves as the permanent anchor.