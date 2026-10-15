# Custom Metrics

| Name in code | Name in local Prometheus | Type | Class |
|---|---|---|---|
| `projectchaos.users.count` | `projectchaos_users_count` | Gauge | `features/user/metrics/CustomUserMetrics` |
| `projectchaos.projects.count` | `projectchaos_projects_count` | Gauge | `features/project/metrics/CustomProjectMetrics` |
| `projectchaos.tasks.count` | `projectchaos_tasks_count` | Gauge | `features/task/metrics/CustomTaskMetrics` |
| `projectchaos.tasks.created` | `projectchaos_tasks_created_total` | Counter | `features/task/CustomTaskRepository` |
| `projectchaos.ai.time_to_first_token` | `projectchaos_ai_time_to_first_token_milliseconds_*` | Timer with histogram | `ai/chat/AIChatService` |
| `projectchaos.ai.time_to_message_completion` | `projectchaos_ai_time_to_message_completion_milliseconds_*` | Timer with histogram | `ai/chat/AIChatService` |

## projectchaos.users.count

Current number of rows in the users table.

## projectchaos.projects.count

Current number of rows in the projects table.

## projectchaos.tasks.count

Current number of rows in the tasks table, including archived tasks.

## projectchaos.tasks.created

Number of tasks created since the application start.
The counter is incremented after the transaction in `CustomTaskRepository.createTask` is committed.

## projectchaos.ai.time_to_first_token

Time from the start of `AIChatService.chat` to the first element of the answer stream.

## projectchaos.ai.time_to_message_completion

Time from the start of `AIChatService.chat` to the end of the answer stream.
