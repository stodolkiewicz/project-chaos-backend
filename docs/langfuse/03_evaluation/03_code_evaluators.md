# Code evaluators

## Context

A code evaluator is a Python or TypeScript function that Langfuse runs on the observations that match a rule.
The function receives the data of one observation and returns one or more scores.

Use a code evaluator for objective checks:

- exact match and regular expressions,
- JSON parseability and schema validation,
- keyword checks and tool-call checks,
- custom business rules.

Use [LLM-as-a-Judge](02_llm_as_a_judge.md) when the check needs semantic judgment, for example tone or helpfulness.

Targets and rules work as for LLM-as-a-Judge.

## Creating a code evaluator

1. Open **Evaluators > New evaluator** and choose the tab **Code evaluator**.
2. Select Python or TypeScript.
3. Write the function `evaluate` in the editor.
4. Test the function in the panel on the right: filter the sample observations, select one and click **Run test on this sample**.
5. Save the evaluator and attach it to a rule.

There is no variable-mapping step.
The function reads the data it needs from the argument `ctx`.

## Function contract

Langfuse calls `evaluate(ctx)` and expects an `EvaluationResult` with a list of scores.

| Field                                                       | Content                                                                                   |
|-------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| `ctx.observation.input`                                     | the input of the matched observation                                                      |
| `ctx.observation.output`                                    | the output of the matched observation                                                     |
| `ctx.observation.metadata`                                  | the metadata of the matched observation                                                   |
| `ctx.observation.tool_calls` (Python), `toolCalls` (TypeScript) | the ordered tool calls with `id`, `name`, `arguments`, `type` and `index`             |
| `ctx.experiment`                                            | set only when the evaluator runs on an experiment, otherwise `None` or `undefined`        |

A score has these fields: `name` and `value` (required), `data_type` (`NUMERIC`, `CATEGORICAL`, `BOOLEAN` or `TEXT`), `comment` (optional), `config_id` (optional) and `metadata` (optional).
The function must return at least one score.

## Example

The function checks that the output of an observation has text.
A model call that only requested a tool has the output `[]`, so it gets `False`.

```python
def evaluate(ctx: EvaluationContext) -> EvaluationResult:
  """Checks that the observation has answer text in its output."""
  output = ctx.observation.output
  has_text = output is not None and str(output).strip() not in ("", "[]")

  return EvaluationResult(
    scores=[
      Score(
        name="has_answer_text",
        value=has_text,
        data_type="BOOLEAN",
        comment=(
          "The output contains answer text."
          if has_text
          else "The output is empty."
        ),
      )
    ]
  )
```

The function does not judge whether the answer is good or on topic.

## Runtime limits

Langfuse runs the function in a sandbox.

| Limit          | Value                                                                  |
|----------------|------------------------------------------------------------------------|
| Libraries      | the standard library of the language only, no third-party packages     |
| Network        | no network access                                                      |
| Time           | the function must finish within 2 seconds                              |
| Source size    | below 256 KB                                                           |
| Result size    | below 256 KB                                                           |
| TypeScript     | erasable syntax only: no enums, namespaces, decorators or parameter properties |

A check that needs a third-party library or an API call runs in your own infrastructure.
Your application then sends the result as a score over the REST API (see [Scores](01_scores.md)).

## Debugging

Every execution creates a trace in the internal environment `langfuse-code-eval`.
Internal environments are hidden from the default Tracing view.
Filter the Tracing table by `environment = langfuse-code-eval` or open the trace from the score.

The execution status is `Completed`, `Error` or `Pending`.
The trace of an execution shows the input, the output, the latency, the logs and the error.

## Requirements

The documentation requires that the observations were ingested with an OpenTelemetry-based Langfuse SDK (Python v3+ or JS/TS v4+).
Self-hosted Langfuse needs a configured code evaluator dispatcher.
Code evaluators can also be created over the public API with `type: "code"`.
