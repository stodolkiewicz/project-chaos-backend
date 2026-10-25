# Filtering

## Context

The `traces` pipeline in `otelcol-config.yaml` sends all spans to Tempo and Langfuse, including HTTP, Spring Security and scheduler spans.
Langfuse needs only the AI spans.

## Separate pipeline

A processor acts on all exporters of its pipeline, so a filter in `traces` would also remove spans from Tempo.
The filter therefore lives in a second pipeline, `traces/langfuse`.

## Filter processor

The `filter` processor removes a span when any of its conditions is true.
Conditions are written in OTTL (OpenTelemetry Transformation Language), the collector's expression language.

A condition tests the attributes of a span.
An attribute is a key-value pair on the span, for example `http.request.method = GET`.
`nil` means the attribute is missing, so `attributes["http.request.method"] != nil` is true for HTTP spans and the filter removes them.

## AI span attributes

Spring AI sets the attribute `gen_ai.operation.name` on all spans it creates.
The value is `chat` for a model call, `execute_tool` for a tool call and `framework` for the internal steps of Spring AI:

- `spring_ai chat_client` covers the whole `ChatClient` call.
- `tool_calling` covers the loop: model call, tool call, model call.
- `message_chat_memory` covers reading and writing the chat memory.
- `stream` and `call` are the streaming and the blocking variant of the `ChatClient` call.

```
chat gpt-5.4-mini                  gen_ai.operation.name = chat
execute_tool getAllProjectTasks    gen_ai.operation.name = execute_tool
tool_calling                       gen_ai.operation.name = framework
```

## Pipeline with the filter

The `traces/langfuse` pipeline uses a filter that removes spans without `gen_ai.operation.name`, which drops the HTTP, Spring Security and scheduler spans, and sends the rest to Langfuse.

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

`filter/langfuse` has the form `type/name`: `filter` is the processor type and `langfuse` is a name we chose. The pipeline `traces/langfuse` follows the same pattern.
In the filter, `traces` selects the signal type and `span` holds the conditions on single spans. This `traces` is not the pipeline `traces`.

The filter keeps all `framework` spans, so the trace in Langfuse keeps its tree structure.
