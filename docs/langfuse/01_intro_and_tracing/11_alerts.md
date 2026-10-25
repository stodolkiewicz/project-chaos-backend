# Alerts

## Context

An alert is a rule that Langfuse evaluates periodically.
Each evaluation computes one metric and compares it with a threshold.
The result is a severity. Langfuse can send a notification when the severity changes.

The page Alerts in the menu lists the alerts. The button **New Alert** creates one.
On Langfuse Cloud the number of alerts per organization depends on the plan: 2 (Hobby), 20 (Core), 50 (Pro), 100 (Enterprise).

## Parts of an alert

An alert has three parts.

1. **Metric.** It defines what Langfuse measures.
   - The data source is `Observations` or one of the score types (`Scores (numeric)`, `Scores (categorical)`, `Scores (boolean)`).
   - The metric is an aggregation plus a measure, for example `avg latency`, `count` or `p95 cost`.
   - Filters narrow the data, for example by model name, tags, user ID or environment.
2. **Condition.** It defines when the metric is a problem.
   - The operator is one of `>`, `≥`, `<`, `≤`, `=`, `≠`.
   - The `Alert threshold` is required. Crossing it sets the severity to `ALERT`.
   - The `Warning threshold` is optional. Crossing it sets the severity to `WARNING`.
3. **Window.** It defines how far back each evaluation looks, for example `1 hour`, `1 day` or `1 week`.

Example: the data source `Observations`, filtered by one model, the metric `p95 latency`, the operator `>`, the `Alert threshold` `5000 ms` and the window `1 hour`.
Langfuse computes the p95 latency of the last hour. The severity is `ALERT` when the value is above 5000 ms.

## Severity

The severity changes after every evaluation.

| Severity  | Meaning                                                                         |
|-----------|---------------------------------------------------------------------------------|
| `UNKNOWN` | The alert has not been evaluated yet.                                           |
| `OK`      | The metric is within bounds.                                                    |
| `WARNING` | The metric crossed the warning threshold.                                       |
| `ALERT`   | The metric crossed the alert threshold.                                         |
| `NO_DATA` | The query returned no data and the no-data mode records it (see below).         |
| `PAUSED`  | The alert is paused, so Langfuse runs no evaluations.                           |

## Advanced settings

### No-data handling

This setting defines what happens when the window contains no matching observations.

| Mode                                  | Behavior                                                             |
|---------------------------------------|----------------------------------------------------------------------|
| **Treat missing data as 0** (default) | Langfuse uses `0` as the value and compares it with the thresholds.  |
| **Keep the previous severity**        | The severity does not change and Langfuse sends no notification.     |
| **Show severity NO_DATA**             | The severity is `NO_DATA` and Langfuse sends no notification.        |
| **Notify after sustained NO_DATA**    | The severity is `NO_DATA` and Langfuse notifies after a set delay.   |

### Renotify

This setting defines whether Langfuse repeats a notification while the severity stays elevated.

- `Off` (default): Langfuse notifies once on each severity transition.
- `Every N minutes`: Langfuse repeats the notification every N minutes (1 to 10080) while the severity is `WARNING` or `ALERT`.

## When notifications are sent

- A breach (`UNKNOWN` or `OK` to `WARNING` or `ALERT`) always sends a notification.
- A recovery (`WARNING` or `ALERT` to `OK`) always sends a notification.
- A change from or to `NO_DATA` sends a notification only in the mode **Notify after sustained NO_DATA**.
- A sustained severity (`WARNING` to `WARNING`, `ALERT` to `ALERT`) sends a notification only when Renotify is on.

## Automations

An alert sends notifications through automations.
An automation pairs a trigger (a severity change of an alert) with an action.

| Action             | What it does                                           |
|--------------------|--------------------------------------------------------|
| **Slack**          | Posts an alert message to a Slack channel.             |
| **Webhook**        | Sends an HTTP POST with an HMAC-signed JSON payload.   |
| **GitHub Actions** | Fires a `workflow_dispatch` event on a repository.     |

Create an automation on the page Automations with the event source **Alert**.
Then select it in the notification section of the alert editor.
Creating, editing and deleting automations requires the role Admin or Owner.
After 5 consecutive delivery failures Langfuse disables the trigger of the automation.

## Pause and delete

The row actions menu of an alert has **Pause** and **Resume**. A paused alert skips all evaluations.
The trash icon on the detail page deletes the alert permanently.
