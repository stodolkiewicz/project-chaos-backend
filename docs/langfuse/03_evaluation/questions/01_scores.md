# Questions: Scores

Lesson: [Scores](../01_scores.md)

## 1. Which objects can a score refer to?

<details>
<summary>Answer</summary>

A score refers to exactly one of these objects: a trace, an observation, a session or a dataset run.

</details>

## 2. How does the body of a score differ when it evaluates one observation and not the whole answer?

<details>
<summary>Answer</summary>

The body contains the `traceId` and also the `observationId`. A score with only the `traceId` evaluates the whole answer.

</details>

## 3. Which data type fits a thumbs up or thumbs down, and which value does the API expect?

<details>
<summary>Answer</summary>

The type is `BOOLEAN`. The value is a number: `1` is true and `0` is false.

</details>

## 4. Which score types do LLM-as-a-Judge and score analytics work with?

<details>
<summary>Answer</summary>

They work with `NUMERIC`, `CATEGORICAL` and `BOOLEAN`.

</details>

## 5. What does the field `source` show, and which values does it take?

<details>
<summary>Answer</summary>

It shows who added the score. `API` means a call from an application, `EVAL` means an automatic evaluation and `ANNOTATION` means a human in the UI.

</details>

## 6. Why does the backend send the user feedback score to Langfuse?

<details>
<summary>Answer</summary>

The call `POST /api/public/scores` needs the secret key, and the backend holds it.

</details>

## 7. How does the backend get the `traceId` of an answer, and where in the code does it read it?

<details>
<summary>Answer</summary>

It reads `tracer.currentSpan().context().traceId()` in the controller method that handles the request. Only there the span of the HTTP request is current.

</details>

## 8. When does a repeated request replace a score and when does it create a new one?

<details>
<summary>Answer</summary>

It replaces the score when the `id`, the `name` and the date of the timestamp are the same. Without an `id`, or when one of the three differs, it creates a new score.

</details>
