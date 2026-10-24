# Collector

## Context

Locally we run `grafana-lgtm` and add a Langfuse exporter to its collector.
To do this we edit the collector config file.
The config file has four parts: receivers, processors, exporters and pipelines.

```yaml
receivers: ...
processors: ...
exporters: ...
service:
  pipelines: ...
```

This file explains each part, so the config is readable.

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
