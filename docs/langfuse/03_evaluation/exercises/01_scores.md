# Exercises: Scores

Lesson: [Scores](../01_scores.md)

## Exercise 1: Create a score over the REST API

1. Open **Tracing** in Langfuse and copy the trace id of any trace.
2. Send a `BOOLEAN` score named `exercise_check` with the value `1` and the comment `exercise 1` to that trace with `curl`. Take the keys from the variable `LANGFUSE_AUTH` (see [Local setup](../../01_intro_and_tracing/03_local_setup.md)).
3. Open **Evaluation > Scores** and find the new row.

Questions:

- Which values do the columns `Name`, `Value`, `Comment` and `Source` show for the new row?
- Which button or filter finds the row among the scores of the evaluators?

<details>
<summary>Answer</summary>

The request:

```bash
curl -X POST https://cloud.langfuse.com/api/public/scores \
  -u "pk-lf-...":"sk-lf-..." \
  -H "Content-Type: application/json" \
  -d '{
    "traceId": "<trace id>",
    "name": "exercise_check",
    "value": 1,
    "dataType": "BOOLEAN",
    "comment": "exercise 1"
  }'
```

- `Name` is `exercise_check`, `Comment` is `exercise 1` and `Source` is `API`, because the application called the API.
- A filter on the name `exercise_check` finds the row. The filter `source:EVAL` shows only the scores of evaluators, so the new row is not among them.

</details>

## Exercise 2: Replace a score with an idempotency key

1. Send the request from exercise 1 twice, without the field `id`. Count the rows named `exercise_check` on the page **Scores**.
2. Send a request with `"id": "exercise-2"` and the value `1`. Then send the same request again with the value `0`. Count the rows with the id `exercise-2`.

Question: why do the two experiments give different counts?

<details>
<summary>Answer</summary>

- Experiment 1 gives two more rows. Without an `id` every request creates a new score.
- Experiment 2 gives one row with the value `0`. The second request has the same `id`, the same `name` and the same date, so it replaces the first score.

</details>
