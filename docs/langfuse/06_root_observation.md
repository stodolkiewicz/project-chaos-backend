# Root observation

## Context

The page Tracing in Langfuse shows a table of observations.
An observation is a single span, for example `chat gpt-5.4-mini` or `execute_tool getAllProjectTasks`.
One user request produces many observations, so the table has many rows per request.

## Trace and root observation

A trace is the tree of all observations created by one request.
The root observation is the top of this tree.
It is the only observation without a parent.

In this project the root observation is `spring_ai chat_client`.
It covers the whole `ChatClient` call.
All other observations (`tool_calling`, `chat gpt-5.4-mini`, `stream`, `message_chat_memory`) are its descendants.

## Finding the root observation in the table

The column Trace Name is filled only in the root observation.
In the other rows this column is empty.

The filter Is Root Observation = True in the left panel hides all other rows.
After this filter, one row in the table is one request.
