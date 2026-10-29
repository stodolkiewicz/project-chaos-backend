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
- Put screenshots of the UI in the `images/` folder of the topic. Take them with Playwright and crop them with PIL. Crop out the left sidebar (it shows the email of the user) and any user id.
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
| `docs/langfuse/03_evaluation/02_llm_as_a_judge.md`          | LLM-as-a-Judge: evaluator, rule, filters, cost, results     |
| `docs/langfuse/03_evaluation/03_code_evaluators.md`         | Code evaluators: example function, runtime limits          |
| `docs/langfuse/03_evaluation/04_decision_model_evaluators.md` | Decision model evaluators: question types                  |
| `docs/langfuse/03_evaluation/05_manual_scores.md`           | Score configs and manual scores in the UI                   |
| `docs/langfuse/03_evaluation/06_score_analytics.md`         | The tab Analytics of the page Scores                        |
| `docs/langfuse/03_evaluation/07_annotation_queues.md`       | Annotation queues                                           |
| `docs/langfuse/03_evaluation/08_datasets.md`                | Datasets: creating, adding items, versions                  |

Exercises are in `docs/langfuse/03_evaluation/exercises/` and comprehension questions in `docs/langfuse/03_evaluation/questions/`. Only `01_scores.md` has both so far.

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
- The span tree of a chat request is `spring_ai chat_client` > `message_chat_memory` > `tool_calling` > `stream` > `chat gpt-5.4-mini`. A request with a tool has two `stream` spans and an `execute_tool` span.
- The generation that only requests a tool has the Output `[]`. `ChatModelCompletionContentObservationFilter` writes only the text of the response to `gen_ai.completion`.
- The filter `Tool Call Count = 0` did not remove these generations, because Langfuse does not see tool calls in the recorded output.
- The generations of the title calls have no prompt name. The filter `Prompt Name = system-prompt` selects the chat generations only (24 of 85 in the test).
- `{{input}}` of the evaluator holds the system prompt, the history and the last user message, because `gen_ai.prompt` holds all messages of the model call.
- The evaluator `talk_about_task management only` is attached to the rule `talk about project mgmt`. Its score is true when the assistant behaved correctly: it helped with the active project or redirected an unrelated request.
- Tested: a poem about a cat got `false`, a geography question that was redirected got `true`.
- The Langfuse worker runs the judge, so an evaluator monitors the application and cannot block an answer.
- The environment `langfuse-llm-as-a-judge` is hidden in the Tracing filter while another filter or a short time range excludes its data.
- The code evaluator `has_answer_text` ran (`code_eval_runtime: PYTHON`) on observations that the Java application sent over OTLP. It returned `true` for generations with text and `false` for the output `[]`. The documentation requires an OTel-based SDK, but it did not block these observations. Dawid has not been taught this yet.
- The page Score Configs says that the data type cannot be changed after creation. The name, description, range and categories can be edited.
- The tab Analytics of the Scores page has the selectors First score and Second score, the object type (default All Objects), the time range (default Past 1 day) and the panels Statistics, Trend Over Time, Distribution and Confusion Matrix.
- The button Annotate is in the toolbar below the name of an observation. The panel Annotate opens on the right and lists the score configs after a click on Add score.
- The table in Tracing shows a bar with Add to Annotation Queue and Add to Dataset after you select rows. The toolbar of an observation has the menu Add to with Dataset, Annotation queue and Playground. The clock icon in the dataset header opens Version History. The wizard Run Experiment has the list Dataset Version after the dataset is selected.
- A score from a queue has `source = ANNOTATION` and a `queueId`.
- The wizard Run Experiment shows the warning Invalid configuration when no item of the dataset has the variables of the prompt (for `system-prompt`: `userId`, `projectName`, `projectId`, `projectDescription`, `createdAt`) as top-level `input` keys.

## Not verified

- Tokens and cost reach Langfuse on production without the filter.
- Whether the button `Open in Playground` needs an Input in the ChatML format.
- Whether Langfuse links a generation to a prompt version that does not exist.
- Whether the Sessions list filters the time by the creation time of the session.
- That the label of a prompt points to one version at a time. The docs imply it but do not say it.
- Whether the bean `Tracer` of Micrometer Tracing is available in the application. `micrometer-tracing-bridge-otel` is in the local Maven repository.
- Whether the UI filters scores by the version of a prompt.
- Whether the options **Also run on past observations** and **Actions > Evaluate** work in this project. Both come from the documentation and were not clicked.
- Why the trace panel on the Scores page selects the span `message_chat_memory` first.
- Whether the OpenAI connection of the project (model `gpt-5.4-mini`) works for decision models. The documentation names `gpt-6-luna`.
- The syntax of the Query mode of the rule filter. The Builder works.
- Whether a collector transform that resets `parent_span_id` makes `spring_ai chat_client` a root. It is not needed for the current rule.

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

7. Scores. Done: types, ways to create, REST API, user feedback, the Scores page and the tab Analytics.
8. Evaluators. Done: LLM-as-a-Judge, code evaluators and decision-model evaluators.
9. Human annotation. Manual scores and score configs are done. Annotation queues are done: creating a queue, adding items, processing, keyboard shortcuts (`07_annotation_queues.md`). Dawid skipped managing queues over the API.
10. Datasets. Done: definition, creating in the UI, adding items, versions (`08_datasets.md`). Skipped: SDK and API. The clock icon in the dataset header opens the Version History.
11. Experiments. Done in chat: definition (one pass of the application over all items of a dataset; the result is a run; two runs of one dataset can be compared) and the page Experiments (sidebar section Evaluation, under Datasets; button Run experiment; table columns Name, Item Count, Error Count, Start Time, Dataset, Referenced Prompts, Latency (s), Total Cost ($), Trace Scores, Observation Scores, Experiment Scores; filters by name, dataset, metadata and scores). Dawid's project has no run yet. No note file exists yet. Next lesson: starting a run with Run experiment (wizard steps Prompt & Model > Dataset > Evaluators > Experiment run details > Review).

Also: Dashboards, environments, observation types.

## Next steps

State at the end of the session (2026-10-29). Dawid pauses for 4-5 days. Nothing is committed (commit only on request).

1. Continue Experiments with "dalej": starting a run via **Run experiment** (cards "via User Interface" and "via Webhook"; the wizard steps above), then reading run results and comparing runs. Estimate of the remaining chat messages: Experiments 8-10, Dashboards about 5, environments about 3, observation types about 3. Write the note `09_experiments.md` after the lessons, not before.
2. The question types of decision model evaluators (Choice, Score, Yes/no) were not taught in chat. The section was removed from `04_decision_model_evaluators.md`. Teach it, then add it back. The image `images/decision-model-questions.png` is unused until then. Dawid's own scores `user_frustration` (decision model) and `talk_about_task_management` (judge) are good examples.
3. Spots in the notes that still cite the documentation instead of the real UI. Dawid wants only reality: verify in the UI with Playwright or remove the sentence.
   - `04_decision_model_evaluators.md` line 16 ("The documentation names three options").
   - `07_annotation_queues.md` lines 50, 52, 70 (Complete + next, Back, Skip, the shortcut table).
   - `08_datasets.md` lines 37-38 (Upload CSV content, link of an item to its trace).
   - Sentences "not verified" remain in `01_scores.md` (about `@langfuse/browser`) and in `02_llm_as_a_judge.md` (three). Ask Dawid what to do with them.
4. Exercises and questions exist only for `01_scores.md`. Create them for the other notes when Dawid asks.
5. Dawid may want a thumbs up and down for the chat. It needs the `traceId` in the SSE stream, an endpoint in the backend and a call of `POST /api/public/scores`. Nothing of this is implemented.
6. Check that tokens and cost appear on production without the filter.
7. Capture the content of tool calls in the spans `execute_tool`.

Working rules learned in this session:

- Never act on a bare pasted quote. Ask what Dawid wants.
- Never write "not verified" sentences in notes. Verify read-only in the UI, or leave the fact out.
- Run `notes-review` (skill in `.claude/skills/notes-review`) on new notes. It floods the context, so a subagent that follows the skill is better.
- Playwright: never save anything in the Langfuse project. Keep Dawid's email and user ids out of screenshots. Avoid `browser_find` on tables.

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

- LLM-as-a-Judge: https://langfuse.com/docs/evaluation/evaluation-methods/llm-as-a-judge
- Evaluation concepts (rules, batch evaluation): https://langfuse.com/docs/evaluation/core-concepts
- Code evaluators: https://langfuse.com/docs/evaluation/evaluation-methods/code-evaluators
- Decision model evaluators: https://langfuse.com/docs/evaluation/evaluation-methods/decision-models
- Scores via UI: https://langfuse.com/docs/evaluation/evaluation-methods/scores-via-ui
- Score configs: https://langfuse.com/faq/all/manage-score-configs
- Score analytics: https://langfuse.com/docs/evaluation/scores/score-analytics
- Annotation queues: https://langfuse.com/docs/evaluation/evaluation-methods/annotation-queues
- Advanced SDK features (span filter, sampling): https://langfuse.com/docs/observability/sdk/advanced-features
- Google ADK integration: https://langfuse.com/integrations/frameworks/google-adk

The pages for experiments are not listed yet.
Find them in the index https://langfuse.com/llms-docs.txt.
