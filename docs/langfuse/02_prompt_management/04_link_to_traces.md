# Linking prompts to traces

## Context

Linking marks a generation with the prompt version that produced it.
The generation then shows the prompt in the UI.
The tab Metrics of the prompt shows these metrics for each version:

- the median latency of a generation,
- the median input tokens and output tokens,
- the median cost,
- the number of generations,
- the median value of the scores,
- the timestamps of the first and the last generation.

## Linking over OpenTelemetry

An application that sends spans over OTLP sets two span attributes:

- `langfuse.observation.prompt.name`: the name of the prompt, a string.
- `langfuse.observation.prompt.version`: the version of the prompt, an integer.

The attributes work only on an observation of the type generation.
The prompt with this name and version has to exist in the project.

## Linking with Spring AI

The span of the generation (`chat gpt-5.4-mini`) belongs to the `ChatModelObservationContext`.
This context has the request `Prompt` with its messages, but not the params of the `ChatClient`.
The metadata of a message is available in the request.

1. The application sets the prompt name and the prompt version as metadata of the system message: `.system(spec -> spec.metadata(key, value))`.
2. An `ObservationFilter` reads the metadata from `getRequest().getInstructions()`.
3. The filter adds the two attributes to the observation. The OTLP exporter sends them with the span.

A call without this metadata, for example the call that generates a title, stays unlinked.
