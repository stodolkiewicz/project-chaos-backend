# Manual scores in the UI

## Context

A team member can score a trace, a session or an observation by hand in the Langfuse UI.
The score gets `source = ANNOTATION`, so it differs from the scores of evaluators (`EVAL`) and of the API (`API`).

Manual scores serve three purposes:

- several reviewers check the data from different points of view,
- a score config keeps the criteria the same for everyone,
- the human scores are a baseline for other scores, for example to check an LLM judge (see [Score analytics](06_score_analytics.md)).

## Score config

The UI needs at least one score config (see [Scores](01_scores.md)) before you can score by hand.
A score config is optional for scores that your code sends over the API.

Create a config in **Project Settings > Scores / Evaluation > Create new score config**.
Fill in:

- the score name,
- the data type: `NUMERIC`, `CATEGORICAL`, `BOOLEAN` or `TEXT`,
- the constraints: minimum and maximum for numeric, the categories for categorical.

Example: the config `answer_quality` with the type `CATEGORICAL` and the categories `good`, `ok` and `bad`.

You can archive a config so that it disappears from the selection for annotation.
Restore it with the same icon.
The documentation describes configs as immutable and also describes editing them with the edit icon.
Old scores stay unchanged after an edit, and new scores must satisfy the current schema.

## Scoring by hand

1. Open a trace, a session or an observation in **Tracing**.
2. Click **Annotate** in the top right corner of the detail panel.
3. Select a score config.
4. Set the value.
5. Add a comment (optional).

The score appears on the tab **Scores** of the same trace, session or observation.

## Scoring experiment results

The compare view of an experiment accepts manual scores.
Experiments are a later lesson.

## Comparing with an evaluator

Compare a human score with an evaluator score only when both have the same data type.
To check a boolean LLM judge, create a score config of the type `BOOLEAN`.
