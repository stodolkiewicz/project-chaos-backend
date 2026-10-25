# Users

## Context

The `userId` is a string that identifies a user of the application, for example a username or a UUID.
The `userId` is optional.
Langfuse uses it to aggregate metrics such as cost per user.

An application that sends spans over OTLP sets the `userId` in the span attribute `langfuse.user.id`.
Set it on the root span, the same way as the `sessionId`, see [09_sessions.md](09_sessions.md).

## Page Users

The page Users in the menu has two views:

- The list of all users shows the number of traces, the token usage and the feedback of each user.
- The view of one user shows aggregated metrics and all traces of this user.

The URL of the view of one user is `https://<hostname>/project/{projectId}/users/{userId}`.
