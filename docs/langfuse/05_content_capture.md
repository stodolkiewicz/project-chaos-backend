# Content capture

## Context

Langfuse shows the columns Input and Output for every observation.
These columns are empty by default.
Spring AI does not put prompts and model answers into spans until you enable it.

![Empty Input and Output columns in the Langfuse trace table](images/05_empty_input_output.png)

## Prompt

The property `spring.ai.chat.observations.log-prompt` decides whether the prompt sent to the model is stored in the span `chat gpt-5.4-mini`.
Langfuse shows this prompt in the column Input.
The default value is `false`.

The file `application-dev.yml` enables the property:

```yaml
spring:
  ai:
    chat:
      observations:
        log-prompt: true
```

The file `application-prod.yml` does not contain the property.
Production keeps the default value `false`, so prompts do not leave the backend.
