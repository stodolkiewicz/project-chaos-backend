# Filtering

## Context

The `traces` pipeline in `otelcol-config.yaml` sends all spans to Tempo and to Langfuse.
Langfuse receives also the HTTP, Spring Security and scheduler spans.
Langfuse needs only the AI spans.

## Separate pipeline

A processor acts on all exporters of its pipeline.
A filter in the `traces` pipeline would also remove spans from Tempo.

We use two pipelines for traces:

- `traces` sends all spans to Tempo.
- `traces/langfuse` filters the spans and sends them to Langfuse.


## Filter processor

The `filter` processor removes spans that match a condition.
The condition is written in OTTL (OpenTelemetry Transformation Language), an expression language of the collector.
The processor removes a span when any of its conditions is true.

This example removes every span that has the attribute `http.request.method`:

```yaml
processors:
  filter/langfuse:
    traces:
      span:
        - 'attributes["http.request.method"] != nil'
```

The name `filter/langfuse` follows the same `type/name` syntax as `traces/langfuse`.

## AI span attributes

Spring AI sets the attribute `gen_ai.operation.name` on all spans it creates.
The value is `chat` for a model call and `execute_tool` for a tool call.
The value is `framework` for the internal steps of Spring AI:

- `spring_ai chat_client` covers the whole `ChatClient` call.
- `tool_calling` covers the loop: model call, tool call, model call.
- `message_chat_memory` covers reading and writing the chat memory.
- `stream` and `call` are the streaming and the blocking variant of the `ChatClient` call.

```
chat gpt-5.4-mini                  gen_ai.operation.name = chat
execute_tool getAllProjectTasks    gen_ai.operation.name = execute_tool
tool_calling                       gen_ai.operation.name = framework
```

HTTP, Spring Security and scheduler spans do not have this attribute.
A filter can use it to separate AI spans from the other spans.

## Pipeline with the filter

The filter removes spans that have no `gen_ai.operation.name`.
The `traces/langfuse` pipeline uses this filter and sends the remaining spans to Langfuse.

```yaml
processors:
  filter/langfuse:
    traces:
      span:
        - 'attributes["gen_ai.operation.name"] == nil'

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [otlp_http/traces]
    traces/langfuse:
      receivers: [otlp]
      processors: [filter/langfuse, batch]
      exporters: [otlp_http/langfuse]
```

The spans `spring_ai chat_client`, `tool_calling`, `stream`, `call` and `message_chat_memory` have `gen_ai.operation.name = framework`.
The filter keeps them, so the trace in Langfuse keeps its tree structure.
