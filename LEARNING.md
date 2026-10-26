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
- Use short sentences with a verb. Do not write fragments.
- When Dawid says an explanation is unclear, rewrite it from the beginning, not as a patch.
- Send the lesson in the chat first. Write the notes in `docs/langfuse/` afterwards.
- The notes are general learning material. They do not describe the application or its profiles.
- Each topic has its own folder in `docs/langfuse/`.
- Check claims against the docs (MCP server `langfuse-docs`) or the API before teaching. Several earlier claims were wrong and had to be corrected.
- Dawid does not use a Langfuse SDK. The application sends spans over OTLP and calls the public REST API directly.
- Name what is not verified. Say "nie sprawdzałem" instead of guessing.

## Setup

- The app runs locally with the `dev` profile, started from IntelliJ.
- The app sends traces to `localhost:4318`.
- The collector runs in the container `project-chaos-backend-grafana-lgtm-1`.
- The file `otelcol-config.yaml` configures the collector. `compose.yml` mounts it.
- Langfuse Cloud (EU) receives the AI traces. The project is `project-chaos`, the organization is `aidave` (plan Hobby).
- The variable `LANGFUSE_AUTH` is the base64 value of `publicKey:secretKey`. The collector and the application use it as the Basic Auth value.
- The block `app.langfuse` in `application-dev.yml` configures the prompt fetching: `base-url`, `auth`, `prompt-label` and `cache-ttl-seconds`.
- Versions: Spring Boot 4.0.8, Spring AI 2.0.1, model `gpt-5.4-mini`.
- Dawid saves screenshots in `~/Pictures/Screenshots`. Read the newest one when he mentions a screenshot.
- Claude can open the Langfuse UI with the Playwright MCP server. The folder `.playwright-mcp/` is in `.gitignore`.
- Claude can read the data with the MCP server `langfuse` (for example `listObservations`).

## Lessons done

| File                                                        | Topic                                                       |
|-------------------------------------------------------------|-------------------------------------------------------------|
| `docs/langfuse/00_mcp_servers.md`                           | MCP servers of Langfuse                                     |
| `docs/langfuse/01_intro_and_tracing/01_overview.md`         | What Langfuse is, trace and span                            |
| `docs/langfuse/01_intro_and_tracing/02_collector.md`        | Collector, pipeline, receiver, processor, exporter          |
| `docs/langfuse/01_intro_and_tracing/03_local_setup.md`      | Local collector config and the way of a trace               |
| `docs/langfuse/01_intro_and_tracing/04_filtering.md`        | Filter processor, only AI spans go to Langfuse              |
| `docs/langfuse/01_intro_and_tracing/05_content_capture.md`  | Input and Output through an `ObservationFilter`             |
| `docs/langfuse/01_intro_and_tracing/06_root_observation.md` | Root observation and how to find it in the table            |
| `docs/langfuse/01_intro_and_tracing/07_usage_and_cost.md`   | Usage details, cost details, model definitions              |
| `docs/langfuse/01_intro_and_tracing/08_timeline.md`         | The Timeline tab                                            |
| `docs/langfuse/01_intro_and_tracing/09_sessions.md`         | Sessions and how the filter gets the ids from the request   |
| `docs/langfuse/01_intro_and_tracing/10_users.md`            | Users                                                       |
| `docs/langfuse/01_intro_and_tracing/11_alerts.md`           | Alerts                                                      |
| `docs/langfuse/02_prompt_management/01_overview.md`         | Prompt management, fetching over the API, caching           |
| `docs/langfuse/02_prompt_management/02_versions_and_labels.md` | Versions, labels, deployment and rollback                |
| `docs/langfuse/02_prompt_management/03_types_and_variables.md` | Prompt types `text` and `chat`, variables `{{...}}`      |
| `docs/langfuse/02_prompt_management/04_link_to_traces.md`   | Linking prompts to generations                              |
| `docs/langfuse/02_prompt_management/05_playground.md`      | Playground and LLM connections                              |
| `docs/langfuse/03_evaluation/01_scores.md`                  | Scores: data types, ways to create, REST API, user feedback |

## Code changes made during the lessons

- `ChatModelCompletionContentObservationFilter` in the package `config` adds `gen_ai.prompt` and `gen_ai.completion`. It has `@Profile("dev")`.
- `ChatClientSessionUserObservationFilter` adds `langfuse.session.id` (the conversation id) and `langfuse.user.id` to the span `spring_ai chat_client`. `AIChatService` passes the user id as the advisor param `userId`.
- `ChatModelPromptLinkObservationFilter` adds `langfuse.observation.prompt.name` and `langfuse.observation.prompt.version` to the span `chat gpt-5.4-mini`. It reads them from the metadata of the system message.
- `LangfusePromptService` in `ai/features/prompt` fetches a text prompt by name from the Langfuse API.
  - It is a singleton bean. The cache holds one entry per prompt name (TTL 60 seconds by default).
  - One lock per prompt name prevents parallel fetches of the same prompt.
  - It replaces `{{name}}` with `{name}`, the template syntax of Spring AI.
  - It returns the stale prompt on an error, and the fallback file when nothing is cached or `LANGFUSE_AUTH` is empty.
- `AIChatService` calls `getTextPrompt("system-prompt", fallbackFile)`. It sets the prompt name and version as metadata of the system message only when the prompt comes from Langfuse.
- `application-dev.yml` sets the four `log-prompt` and `log-completion` properties to `false`, with comments. It also holds the block `app.langfuse`.
- `otelcol-config.yaml` has the pipeline `traces/langfuse` with the processor `filter/langfuse`.
- `.gitignore` ignores `.playwright-mcp/`.

## Verified facts

- `spring.ai.chat.observations.log-prompt` and `log-completion` only enable handlers that write to the application log.
- The filter reads the prompt and the completion directly from `ChatModelObservationContext`. The properties are not needed for it.
- Langfuse reads Input from `gen_ai.prompt` and Output from `gen_ai.completion`.
- A chat request creates the span `spring_ai chat_client` with a parent. The parent is the span of the HTTP request, which the collector filter does not forward. Langfuse does not treat `spring_ai chat_client` as a root observation, so the filter `Is Root Observation = True` shows none of the chat traces (also on the page of a user).
- The call that generates the conversation title is a separate trace. Its `spring_ai chat_client` has no parent and no session or user.
- The pages Sessions and Users show the chat traces. A session is one conversation (`sessionId` = `conversationId`).
- `ChatModelObservationContext` has no access to the params of the `ChatClient`. The metadata of a message is available in `getRequest().getInstructions()`.
- Prompt linking works: the generation shows `prompt: system-prompt - v2`, and the API returns `promptName` and `promptVersion`. A new version with the label `production` reaches the application within the TTL, without a restart.
- The observation filter of the API has the fields `promptName` and `promptVersion`.
- The generation `chat gpt-5.4-mini` has no button `Open in Playground`. Its Input is one string from `gen_ai.prompt`, so Langfuse does not see messages with roles or tool definitions.
- One request with several tools creates one `chat gpt-5.4-mini` span per model call.

## Not verified

- Tokens and cost reach Langfuse on production without the filter.
- Whether the button `Open in Playground` needs an Input in the ChatML format.
- Whether Langfuse links a generation to a prompt version that does not exist.
- Whether the Sessions list filters the time by the creation time of the session.
- That the label of a prompt points to one version at a time. The docs imply it but do not say it.
- Whether the bean `Tracer` of Micrometer Tracing is available in the application. `micrometer-tracing-bridge-otel` is in the local Maven repository.
- Whether the UI filters scores by the version of a prompt.

## Curriculum

Observability (done):

1. Tracing: tokens and cost, reading a trace, the timeline, filters.
2. Sessions.
3. Users.
4. Alerts.

Prompt management (done):

5. Prompts: versioned prompts managed in Langfuse.
6. Playground.

Evaluation:

7. Scores. In progress: types, ways to create, REST API and user feedback are done.
8. Evaluators: LLM-as-a-judge (next).
9. Human annotation.
10. Datasets.
11. Experiments.

Also: Dashboards, environments, observation types.

## Next steps

1. Continue with the ways to create scores: LLM-as-a-Judge, code evaluators, scores in the UI, annotation queues.
2. Dawid may want a thumbs up and down for the chat. It needs the `traceId` in the SSE stream, an endpoint in the backend and a call of `POST /api/public/scores`. Nothing of this is implemented.
3. Check that tokens and cost appear on production without the filter.
4. Capture the content of tool calls in the spans `execute_tool`.

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
- Alerts: https://langfuse.com/docs/observability/features/alerts
- Best practices for traces: https://langfuse.com/docs/observability/best-practices
- Prompt management: https://langfuse.com/docs/prompt-management/overview
- Prompt data model: https://langfuse.com/docs/prompt-management/data-model
- Prompt version control: https://langfuse.com/docs/prompt-management/features/prompt-version-control
- Linking prompts to traces: https://langfuse.com/docs/prompt-management/features/link-to-traces
- Playground: https://langfuse.com/docs/prompt-management/features/playground
- LLM connections: https://langfuse.com/docs/administration/llm-connection
- Scores overview: https://langfuse.com/docs/evaluation/scores/overview
- Scores data model: https://langfuse.com/docs/evaluation/scores/data-model
- Scores via API and SDK: https://langfuse.com/docs/evaluation/evaluation-methods/scores-via-sdk
- Datasets: https://langfuse.com/docs/evaluation/experiments/datasets
- Experiments via OpenTelemetry: https://langfuse.com/docs/evaluation/experiments/experiments-via-opentelemetry
- Existing OpenTelemetry setup: https://langfuse.com/faq/all/existing-otel-setup
- Tracing academy: https://langfuse.com/academy/tracing
- Monitoring academy: https://langfuse.com/academy/monitoring
- API reference: https://api.reference.langfuse.com
- Langfuse CLI: https://langfuse.com/docs/api-and-data-platform/features/cli
- Langfuse agent skill: https://langfuse.com/docs/api-and-data-platform/features/agent-skill

The pages for LLM-as-a-Judge, annotation queues and experiments are not listed yet.
Find them in the index https://langfuse.com/llms-docs.txt.
