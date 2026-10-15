# Prometheus: `_sum`, `_count` and average time

## Metric suffixes

- `_milliseconds` / `_seconds` = unit in the name, not a metric type.
- `_bucket`, `_count`, `_sum` = the three series a Micrometer timer exports.

| Metric | Returns |
|--------|---------|
| `..._count` | Counter: number of measured messages since app start. Equals bucket `le="+Inf"`. |
| `..._sum` | Counter: total duration of all messages since app start, in the unit from the name. |

Both only grow and reset to 0 on app restart.

Example: 3 messages of 2, 4, 6 s -> `_sum` = 12 s, `_count` = 3.

## Average time (one instance)

```promql
rate(projectchaos_ai_time_to_message_completion_milliseconds_sum[60m])
/
rate(projectchaos_ai_time_to_message_completion_milliseconds_count[60m])
```

| Part | Returns |
|------|---------|
| `rate(..._sum[60m])` | Per-second increase of total time over the last 60 min |
| `rate(..._count[60m])` | Per-second increase of message count over the last 60 min |
| `/` | Average duration of one message in ms over the last 60 min ("per second" cancels out) |

Example: 4 messages of 2, 3, 3, 8 s -> 16 s / 4 = 4 s.

- Each graph point = average of the 60 min before it (sliding window).
- No messages in the window -> 0 / 0 = NaN.
- Average is pulled up by slow outliers, median (`histogram_quantile`) is not. Use both.

## Multiple instances

Same query without changes = one line per instance (each divides its own `_sum` by its own `_count`).

For one average for the whole system, sum numerator and denominator separately:

```promql
sum(rate(..._sum[60m])) / sum(rate(..._count[60m]))
```

| Part | Returns |
|------|---------|
| `sum(rate(..._sum[60m]))` | Total time of all messages from all instances |
| `sum(rate(..._count[60m]))` | Number of messages from all instances |
| `/` | Average per message across the whole system |

Never average the averages:

| Instance | Messages | Total time | Own average |
|----------|----------|------------|-------------|
| A | 1 | 10 s | 10 s |
| B | 9 | 18 s | 2 s |

- Correct (`sum / sum`): (10 + 18) / (1 + 9) = **2.8 s**
- Wrong (average of averages): (10 + 2) / 2 = **6 s**, instance A with 1 message weighs as much as B with 9.

`histogram_quantile` has the same issue, solved by `sum by (le)`: buckets from all instances are added first, quantile is computed after.

# Prometheus: `histogram_quantile` notes

```promql
histogram_quantile(
  0.5,
  sum by (le) (rate(projectchaos_ai_time_to_message_completion_milliseconds_bucket[50m]))
)
```

Result: median message completion time (ms) over the last 50 min.

## Buckets

- Cumulative counters, label `le` = "less or equal" (ms): `le="4000"` counts messages <= 4000 ms.
- `le="+Inf"` = all messages.
- Count since app start, only grow, reset on restart.
- Needs `publishPercentileHistogram()` in Micrometer, otherwise only `+Inf` exists -> NaN.

## What each part returns

| Part | Returns |
|------|---------|
| `..._bucket` | 70 series (one per `le`), value = messages since app start with duration <= `le` |
| `[50m]` | Last 50 min of samples for each series (range vector, not graphable) |
| `rate(...)` | 70 series, value = per-second increase over the window (fresh traffic only, handles resets) |
| `sum by (le)` | One series per `le`: drops other labels, sums series with the same `le` (matters with multiple instances) |
| `histogram_quantile(0.5, ...)` | One number in ms: time below which 50% of messages fall (`0.95` = p95). Estimate, assumes even spread inside a bucket |

## Reading the graph

- Each point = query evaluated at that moment over the preceding 50 min.
- NaN / gap: no new messages in the window, or app down.
- ~30 000 ms: window still holds old data with only `+Inf`.
- Few messages = meaningless median. Check sample size:

```promql
sum(increase(projectchaos_ai_time_to_message_completion_milliseconds_count[50m]))
```
