package com.stodo.projectchaos.ai.tools;

import com.stodo.projectchaos.features.task.TaskService;
import com.stodo.projectchaos.features.task.dto.service.Task;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.UUID;

// The tools only show a proposal to the user. The user performs the change through the regular task endpoints.
@Component
public class TaskProposalTools {

    public static final String EVENTS_KEY = "events";
    public static final String PROJECT_ID_KEY = "projectId";

    private final TaskService taskService;

    public TaskProposalTools(TaskService taskService) {
        this.taskService = taskService;
    }

    @Tool(description = """
            Show the user a form for creating a new task, prefilled with the proposed title and description.
            This tool does NOT create the task. The user may edit, save or cancel the form.
            You never learn whether the task was created.""")
    public String createTask(
            @ToolParam(description = "proposed task title", required = true) String title,
            @ToolParam(description = "proposed task description", required = true) String description,
            ToolContext toolContext
    ) {
        emit(
                toolContext,
                "RequestCreateTask",
                new RequestCreateTaskEventDTO(title, description)
        );
        return "The user was shown a form for creating the task \"" + title + "\". " +
                "It is unknown whether they will save it.";
    }

    @Tool(description = """
            Show the user a confirmation popup for deleting a task.
            This tool does NOT delete the task. The user may confirm or cancel.
            You never learn whether the task was deleted.""")
    public String deleteTask(
            @ToolParam(description = "technical UUID identifier of the task", required = true) UUID taskId,
            ToolContext toolContext
    ) {
        UUID projectId = (UUID) toolContext.getContext().get(PROJECT_ID_KEY);
        Task task = taskService.findTask(
                projectId,
                taskId
        );
        emit(
                toolContext,
                "RequestDeleteTask",
                new RequestDeleteTaskEventDTO(task.id(), task.title())
        );
        return "The user was shown a confirmation popup for deleting the task \"" + task.title() + "\" (" + taskId + "). " +
                "It is unknown whether they will confirm it.";
    }

    @SuppressWarnings("unchecked")
    private void emit(
            ToolContext toolContext,
            String eventName,
            Object data
    ) {
        var sink = (Sinks.Many<ServerSentEvent<Object>>) toolContext.getContext().get(EVENTS_KEY);
        ServerSentEvent<Object> event = ServerSentEvent.builder()
                .event(eventName)
                .data(data)
                .build();
        // busyLooping retries when two tools emit at the same time
        sink.emitNext(
                event,
                Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(100))
        );
    }
}
