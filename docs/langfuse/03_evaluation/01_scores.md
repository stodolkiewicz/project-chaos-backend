# Scores

## Context

A score is an evaluation result stored in Langfuse.
It has a name (for example `user_feedback`), a value and a data type.
An optional comment holds the reasoning or the notes of a reviewer.

A score refers to exactly one of these objects:

- a trace (one interaction, the most common case),
- an observation (one step of a trace, for example one model call),
- a session (a whole conversation),
- a dataset run.

An observation is one span. A trace is the tree of all observations of one request.
A score with a `traceId` evaluates the whole answer. A score with a `traceId` and an `observationId` evaluates one observation.

## Data types

| Type          | Value                                       | Use for                                        |
|---------------|---------------------------------------------|------------------------------------------------|
| `NUMERIC`     | a float, for example `0.9`                  | relevance, similarity, star ratings            |
| `CATEGORICAL` | a string from predefined categories         | `correct`, `partially correct`, `wrong`        |
| `BOOLEAN`     | `0` or `1`                                  | pass or fail checks, thumbs up or down         |
| `TEXT`        | a free string of 1 to 500 characters        | notes of a reviewer                            |

Text scores do not work in experiments, in LLM-as-a-Judge and in score analytics.

A score config defines a schema for scores: the name, the data type and the allowed values.
A config is immutable. It can be archived and restored.

## Source

The field `source` shows who added the score: `API`, `EVAL` (an automatic evaluation) or `ANNOTATION` (a human in the UI).

## Ways to create scores

1. The API or an SDK, called from the application. It fits user feedback and guardrail results.
2. LLM-as-a-Judge: a model evaluates traces automatically.
3. Code evaluators: Python or TypeScript code that runs in Langfuse and checks deterministic rules.
4. The UI: a team member scores a trace, an observation or a session. It requires a score config.
5. Annotation queues: reviewers work through batches of traces.

## Scores or tags

A tag describes what a trace is. It is set while tracing and does not change afterwards.
A score measures how good a trace is. It can be added at any time, also long after the trace was created.

## Creating a score over the REST API

```
POST https://cloud.langfuse.com/api/public/scores
```

The authentication is Basic Auth with the public key as the username and the secret key as the password.
The body is a list of objects:

```json
[
  {
    "traceId": "6f7a7fb6dd3acf8f430fa71520831eef",
    "name": "helpfulness",
    "value": 0,
    "dataType": "BOOLEAN",
    "comment": "Incorrect answer"
  }
]
```

- `observationId` is optional and narrows the score to one observation.
- A `BOOLEAN` value is a number: `1` is true and `0` is false.
- A `CATEGORICAL` value is a string.
- The optional field `id` is an idempotency key. A repeated request with the same `id` replaces the score.

## User feedback from an application

The call needs the secret key, so the backend sends the score and the frontend does not.

1. The backend reads the id of the trace in the thread of the controller, where the span of the HTTP request is current: `tracer.currentSpan().context().traceId()`. The streamed answer runs on other threads.
2. The backend sends the `traceId` to the frontend together with the answer, for example as an SSE event.
3. The user clicks thumbs up or down. The frontend sends the `traceId` and the value to the backend.
4. The backend calls `POST /api/public/scores`.

The library `@langfuse/browser` sends scores from the browser with a separate key that has only the permission to ingest scores.
