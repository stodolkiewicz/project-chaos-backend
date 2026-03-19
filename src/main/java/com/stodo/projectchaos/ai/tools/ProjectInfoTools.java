package com.stodo.projectchaos.ai.tools;

import com.stodo.projectchaos.features.projectuser.ProjectUserService;
import com.stodo.projectchaos.features.projectuser.dto.service.ProjectUser;
import com.stodo.projectchaos.features.task.TaskService;
import com.stodo.projectchaos.features.task.dto.service.BoardTask;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ProjectInfoTools {

    private final ProjectUserService projectUserService;
    private final TaskService taskService;

    public ProjectInfoTools(ProjectUserService projectUserService, TaskService taskService) {
        this.projectUserService = projectUserService;
        this.taskService = taskService;
    }

    @Tool(description = "Get all users assigned to the project")
    public List<ProjectUser> getAllUsersAssignedToTheProject(
            @ToolParam(description = "technical UUID identifier of the project.", required = true) UUID projectId
    ) {
        List<ProjectUser> projectUsers = projectUserService.findProjectUsersByProjectId(projectId);
        return projectUsers;
    }

    @Tool(description = "Get all tasks on the kanban board in the project")
    public List<BoardTask> getAllTasksOnTheKanbanBoard(
            @ToolParam(description = "technical UUID identifier of the project.", required = true) UUID projectId
    ) {
        List<BoardTask> boardTasks = taskService.getBoardTasks(projectId);
        return boardTasks;
    }
}
