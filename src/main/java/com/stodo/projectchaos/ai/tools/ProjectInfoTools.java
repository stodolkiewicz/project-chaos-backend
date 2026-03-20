package com.stodo.projectchaos.ai.tools;

import com.stodo.projectchaos.features.projectuser.ProjectUserService;
import com.stodo.projectchaos.features.projectuser.dto.service.ProjectUser;
import com.stodo.projectchaos.features.task.TaskService;
import com.stodo.projectchaos.features.task.dto.service.BoardTask;
import com.stodo.projectchaos.model.enums.TaskStageEnum;
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
            @ToolParam(description = "technical UUID identifier of the project", required = true) UUID projectId
    ) {
        List<ProjectUser> projectUsers = projectUserService.findProjectUsersByProjectId(projectId);
        return projectUsers;
    }

    @Tool(description = "Get all tasks in the backlog of the project (backlog = stage 1)")
    public List<BoardTask> getAllTasksInTheBacklogOfTheProject(
            @ToolParam(description = "technical UUID identifier of the project", required = true) UUID projectId
    ) {
        List<BoardTask> boardTasks = taskService.getTasks(projectId, TaskStageEnum.BACKLOG);
        return boardTasks;
    }

    @Tool(description = "Get all tasks on the kanban board in the project (kanban board = stage 2)")
    public List<BoardTask> getAllTasksOnTheKanbanBoard(
            @ToolParam(description = "technical UUID identifier of the project", required = true) UUID projectId
    ) {
        List<BoardTask> boardTasks = taskService.getTasks(projectId, TaskStageEnum.BOARD);
        return boardTasks;
    }

    @Tool(description = "Get all project's ARCHIVED tasks. Think of ARCHIVED as done/completed. (ARCHIVED = stage 3)")
    public List<BoardTask> getAllProjectsArchivedTasks(
            @ToolParam(description = "technical UUID identifier of the project", required = true) UUID projectId
    ) {
        List<BoardTask> boardTasks = taskService.getTasks(projectId, TaskStageEnum.ARCHIVED);
        return boardTasks;
    }

    @Tool(description = "Get all tasks in the project. (stage 1, 2 and 3 = backlog, kanban board and archived)")
    public List<BoardTask> getAllProjectTasks(
            @ToolParam(description = "technical UUID identifier of the project", required = true) UUID projectId
    ) {
        List<BoardTask> boardTasks = taskService.getTasks(projectId, null);
        return boardTasks;
    }
}
