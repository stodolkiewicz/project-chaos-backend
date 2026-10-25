# Local Setup

## Context

The app creates a trace for every request, and Spring AI adds spans for model calls and tool calls.
The app sends traces to the collector in the `grafana-lgtm` container (`localhost:4318`).
The collector forwards all spans to Tempo and the AI spans to Langfuse Cloud.
This file walks through the collector config (`otelcol-config.yaml`). The concepts are in [02_collector.md](02_collector.md).

## Pipelines

The `traces` pipeline sends all spans to Tempo.
The `traces/langfuse` pipeline filters the spans and sends only the AI spans to Langfuse.
The filter is explained in [04_filtering.md](04_filtering.md).

```yaml
traces:
  receivers: [otlp]
  processors: [batch]
  exporters: [otlp_http/traces]
traces/langfuse:
  receivers: [otlp]
  processors: [filter/langfuse, batch]
  exporters: [otlp_http/langfuse]
```

The `metrics` and `logs` pipelines never reach Langfuse.

## Receiver

The `otlp` receiver accepts OTLP over HTTP on `4318`.

```yaml
receivers:
  otlp:
    protocols:
      http:
        endpoint: 0.0.0.0:4318
```

## Processor

`batch` collects spans and exports them together, so the exporter sends fewer HTTP requests to Langfuse.

```yaml
processors:
  batch:
```

## Exporter

`Authorization` uses HTTP Basic auth. The login is the Langfuse public key and the password is the secret key.
`LANGFUSE_AUTH` holds both keys as `base64("pk-lf-...:sk-lf-...")` (see the main README).
`${env:LANGFUSE_AUTH}` is collector syntax. The collector replaces it with the value of the environment variable when it reads the config.
`compose.yml` passes the variable from your shell to the `grafana-lgtm` container.
`x-langfuse-ingestion-version` makes Langfuse show the traces in real time. Without it they can appear up to 10 minutes late.

```yaml
exporters:
  otlp_http/langfuse:
    endpoint: https://cloud.langfuse.com/api/public/otel
    headers:
      Authorization: "Basic ${env:LANGFUSE_AUTH}"
      x-langfuse-ingestion-version: "4"
```
