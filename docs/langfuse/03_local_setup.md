# Local Setup

## Context

Locally the app sends traces to the collector in the `grafana-lgtm` container.
The collector forwards them to Tempo and to Langfuse Cloud.
Langfuse shows the AI calls of the app.
This file describes the collector config (`otelcol-config.yaml`) that does this.
The concepts are explained in [02_collector.md](02_collector.md).

## The way of a trace

1. The app creates a trace for every request. Spring AI adds spans for model calls and tool calls.
2. The app sends the trace to `localhost:4318`.
3. The collector receives the trace on this port.
4. The collector sends the trace to Tempo and to Langfuse.

## Pipelines

The `traces` pipeline takes traces from the app and sends them to Tempo and to Langfuse.

```yaml
traces:
  receivers: [otlp]
  processors: [batch]
  exporters: [otlp_http/traces, otlp_http/langfuse]
```

The `metrics` and `logs` pipelines send data to Grafana tools. Langfuse receives the traces.

## Receiver

The receiver is called `otlp` and listens on port `4318`.
This is the port from step 2 of "The way of a trace".

```yaml
receivers:
  otlp:
    protocols:
      http:
        endpoint: 0.0.0.0:4318
```

## Processor

The processor is called `batch`.
It groups spans into batches, so the exporter sends fewer HTTP requests to Langfuse.

```yaml
processors:
  batch:
```

## Exporter

The exporter `otlp_http/langfuse` sends traces to Langfuse Cloud.
`endpoint` is the OTLP address of Langfuse.
`Authorization` uses HTTP Basic auth. The login is the Langfuse public key and the password is the secret key.
`LANGFUSE_AUTH` holds both keys as `base64("pk-lf-...:sk-lf-...")` (see the main README).
`${env:LANGFUSE_AUTH}` is collector syntax. The collector replaces it with the value of the environment variable when it reads the config.
`compose.yml` passes the variable from your shell to the `grafana-lgtm` container.
`x-langfuse-ingestion-version` makes Langfuse show the traces in real time. Without it they can appear up to 15 minutes late.

```yaml
exporters:
  otlp_http/langfuse:
    endpoint: https://cloud.langfuse.com/api/public/otel
    headers:
      Authorization: "Basic ${env:LANGFUSE_AUTH}"
      x-langfuse-ingestion-version: "4"
```
