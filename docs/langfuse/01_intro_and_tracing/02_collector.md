# Collector

## Context

Locally we run `grafana-lgtm`, the compose service built from the image `grafana/otel-lgtm`.
It is an all-in-one observability stack: an OpenTelemetry collector, Tempo (trace storage), Loki (logs), Prometheus (metrics) and Grafana (UI).
We add a Langfuse exporter to its collector by editing the collector config file.
The config file has four parts: receivers, processors, exporters and pipelines.

```yaml
receivers: ...
processors: ...
exporters: ...
service:
  pipelines: ...
```

## Collector

The app produces telemetry: traces, metrics and logs.
The collector is a program that receives this telemetry from the app and forwards it to backends, for example Tempo and Langfuse.
The app sends telemetry only to the collector.
To add a backend, we change the collector config (`otelcol-config.yaml`).

The app hands the telemetry to the collector and continues working.

## Pipeline

A pipeline is the path of data inside the collector, from receivers through processors to exporters.

## Receiver

A receiver listens on a port and takes data into the collector.

## Processor

A processor changes the data or controls its flow between the receiver and the exporter.

## Exporter

An exporter sends data out of the collector to a backend (for example Tempo or Langfuse).
