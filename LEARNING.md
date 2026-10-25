# Learning progress: Langfuse and observability

This file lets a new Claude session continue teaching Dawid.
Read it together with the files in `docs/langfuse/`.

## Goal

Dawid wants to learn the whole of Langfuse, not only tracing.
The curriculum follows the sidebar of the Langfuse UI.
## How to teach

- Talk to Dawid in Polish. Write files and code comments in English.
- Teach one small thing per message. End with a request to write "dalej".
- Define the term first. Then show a concrete example from the project.
- Name every object exactly: the file, the container, the port, the span.
- Do not explain basics. Dawid is a mid/senior engineer.
- When Dawid says an explanation is unclear, rewrite it from the beginning.
- Add every explanation to a file in `docs/langfuse/` right away.
- Check claims before teaching. Two earlier claims were wrong and had to be corrected.

## Setup

- The app runs locally with the `dev` profile, started from IntelliJ.
- The app sends traces to `localhost:4318`.
- The collector runs in the container `project-chaos-backend-grafana-lgtm-1`.
- The file `otelcol-config.yaml` configures the collector. `compose.yml` mounts it.
- Langfuse Cloud (EU) receives the AI traces. The variable `LANGFUSE_AUTH` holds the keys.
- Versions: Spring Boot 4.0.8, Spring AI 2.0.1, model `gpt-5.4-mini`.
- Dawid saves screenshots in `~/Pictures/Screenshots`. Read the newest one when he mentions a screenshot.

## Lessons done

| File                                  | Topic                                              |
|---------------------------------------|----------------------------------------------------|
| `docs/langfuse/01_intro_and_tracing/01_overview.md`        | What Langfuse is, trace and span                   |
| `docs/langfuse/01_intro_and_tracing/02_collector.md`       | Collector, pipeline, receiver, processor, exporter |
| `docs/langfuse/01_intro_and_tracing/03_local_setup.md`     | Local collector config and the way of a trace      |
| `docs/langfuse/01_intro_and_tracing/04_filtering.md`       | Filter processor, only AI spans go to Langfuse     |
| `docs/langfuse/01_intro_and_tracing/05_content_capture.md` | Input and Output through an `ObservationFilter`    |
| `docs/langfuse/01_intro_and_tracing/06_root_observation.md`| Root observation and how to find it in the table   |

## Code changes made during the lessons

- `ChatModelCompletionContentObservationFilter` in the package `config` adds `gen_ai.prompt` and `gen_ai.completion`.
- The filter has `@Profile("dev")`, so production does not send prompts to Langfuse.
- `application-dev.yml` sets the four `log-prompt` and `log-completion` properties to `false`, with comments.
- `otelcol-config.yaml` has the pipeline `traces/langfuse` with the processor `filter/langfuse`.

## Verified facts

- `spring.ai.chat.observations.log-prompt` and `log-completion` only enable handlers that write to the application log.
- The filter reads the prompt and the completion directly from `ChatModelObservationContext`. The properties are not needed for it.
- Langfuse reads Input from `gen_ai.prompt` and Output from `gen_ai.completion`.
- The root observation in this project is `spring_ai chat_client`.
- One request with several tools creates one `chat gpt-5.4-mini` span per model call.

## Not verified

- The sixth model call in a request probably generates the conversation title.
- The title call probably has its own trace with its own root `spring_ai chat_client`.
- Tokens and cost reach Langfuse on production without the filter.

## Curriculum

Observability:

1. Tracing: tokens and cost, reading a trace, the timeline, filters. Started.
2. Sessions: grouping the traces of one conversation.
3. Users: assigning traces to users.
4. Alerts.

Prompt management:

5. Prompts: versioned prompts managed in Langfuse.
6. Playground.

Evaluation:

7. Scores.
8. Evaluators: LLM-as-a-judge.
9. Human annotation.
10. Datasets.
11. Experiments.

Also: Dashboards, environments, observation types.

## Next steps in tracing

1. Teach tokens and cost. Show the attributes `gen_ai.usage.*` and where Langfuse shows them.
2. Check that tokens and cost appear on production without the filter.
3. Add a second `ObservationFilter` for `ChatClientObservationContext`. It fills Input and Output of the root.
4. Capture the content of tool calls in the spans `execute_tool`.
5. Teach how to read a trace: open the root, read the timeline, find slow steps.

## Links

Use the MCP server `langfuse-docs` first. Its tools are `searchLangfuseDocs` and `getLangfuseDocsPage`.
Append `.md` to a docs URL to get plain Markdown.

- Index of all pages: https://langfuse.com/llms.txt
- Docs index: https://langfuse.com/llms-docs.txt
- Integrations index: https://langfuse.com/llms-integrations.txt
- OpenTelemetry endpoint and attribute mapping: https://langfuse.com/integrations/native/opentelemetry
- Spring AI integration: https://langfuse.com/integrations/frameworks/spring-ai
- Spring AI demo application: https://github.com/langfuse/langfuse-examples/tree/main/applications/spring-ai-demo
- Data model: https://langfuse.com/docs/observability/data-model
- Observation types: https://langfuse.com/docs/observability/features/observation-types
- Environments: https://langfuse.com/docs/observability/features/environments
- Best practices for traces: https://langfuse.com/docs/observability/best-practices
- Experiments via OpenTelemetry: https://langfuse.com/docs/evaluation/experiments/experiments-via-opentelemetry
- Existing OpenTelemetry setup: https://langfuse.com/faq/all/existing-otel-setup
- Tracing academy: https://langfuse.com/academy/tracing
- Monitoring academy: https://langfuse.com/academy/monitoring
- API reference: https://api.reference.langfuse.com
- Langfuse CLI: https://langfuse.com/docs/api-and-data-platform/features/cli
- Langfuse agent skill: https://langfuse.com/docs/api-and-data-platform/features/agent-skill

The pages for Sessions, Users, Prompts, Scores, Datasets and Experiments are not listed yet.
Find them in the index https://langfuse.com/llms-docs.txt.
