# Sessions

## Context

A session is a group of traces that share one `sessionId`, for example all traces of one chat conversation.

## sessionId

The `sessionId` is any US-ASCII string shorter than 200 characters.
Langfuse drops a longer value.

## sessionId over OpenTelemetry

An application that sends spans over OTLP sets the `sessionId` in the span attribute `langfuse.session.id`.
Set it on the root span (`spring_ai chat_client`, see [06_root_observation.md](06_root_observation.md)), as in this example:

```java
Span span = tracer.spanBuilder("user-interaction")
    .setAttribute("langfuse.session.id", sessionId)
    .startSpan();
```

## How the filter gets the ids from the request

Spring AI creates the root span `spring_ai chat_client`, so the application cannot set attributes on it directly.
An `ObservationFilter` (a hook that changes an observation before the export, see [05_content_capture.md](05_content_capture.md)) adds them.
The filter reads the ids from the map `context` of the `ChatClientRequest`.

1. The application passes the ids as advisor params: `.advisors(spec -> spec.param(CONVERSATION_ID, conversationId).param(USER_ID, userId))`.
   Spring AI stores the params in the map `context` of the `ChatClientRequest`.
   The same map goes to the advisors. `MessageChatMemoryAdvisor` reads `CONVERSATION_ID` from it to load the history of the right conversation.
2. At the start of a `ChatClient` call Spring AI creates a `ChatClientObservationContext`.
   This object holds a reference to the whole `ChatClientRequest`, available through `getRequest()`.
3. When the observation ends, Micrometer passes its context through all beans of type `ObservationFilter`.
   Spring Boot registers these beans automatically, so the filter needs no extra configuration.
4. The filter calls `getRequest().context().get(...)` and adds the values as span attributes.
   The OTLP exporter sends the attributes together with the span.

The same mechanism sets `langfuse.user.id`, see [10_users.md](10_users.md).

## Page Sessions

Opening a session on the page Sessions replays the whole interaction, trace by trace.

Without any `sessionId` the page shows the message `Start using sessions`.
