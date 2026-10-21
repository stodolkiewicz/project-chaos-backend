package com.stodo.projectchaos.integration.repository;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.project.ColumnNotEmptyException;
import com.stodo.projectchaos.features.project.CustomProjectRepository;
import com.stodo.projectchaos.features.project.ProjectService;
import com.stodo.projectchaos.features.project.dto.request.UpdateColumnRequestDTO;
import com.stodo.projectchaos.features.project.dto.request.UpdateProjectRequestDTO;
import com.stodo.projectchaos.integration.repository.config.TestContainersBase;
import com.stodo.projectchaos.integration.TestcontainersPostgresConfiguration;
import com.stodo.projectchaos.integration.TcHelper;
import com.stodo.projectchaos.model.enums.ColumnColorEnum;
import com.stodo.projectchaos.model.enums.TaskStageEnum;
import com.stodo.projectchaos.storage.AttachmentManager;
import com.stodo.projectchaos.storage.projectlimit.ProjectLimitService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({
        TestcontainersPostgresConfiguration.class,
        TcHelper.class,
        ProjectService.class
})
class UpdateProjectTCTest extends TestContainersBase {

    @MockitoBean
    CustomProjectRepository customProjectRepository;

    @MockitoBean
    ProjectLimitService projectLimitService;

    @MockitoBean
    AttachmentManager attachmentManager;

    @MockitoBean(name = "applicationTaskExecutor")
    Executor applicationTaskExecutor;

    @Autowired
    TcHelper tcHelper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ProjectService projectService;

    @Autowired
    EntityManager entityManager;

    private UUID projectId;
    private UUID todoId;
    private UUID doingId;
    private UUID doneId;

    @BeforeEach
    void setUp() {
        projectId = tcHelper.createProject("project");
        todoId = createColumn("To do", 0, ColumnColorEnum.GRAY);
        doingId = createColumn("In progress", 100, ColumnColorEnum.BLUE);
        doneId = createColumn("Done", 200, ColumnColorEnum.GREEN);
    }

    @Test
    public void updateProject_shouldChangeDescriptionNamesAndOrder() {
        // given
        UpdateProjectRequestDTO requestDTO = new UpdateProjectRequestDTO(
                "new description",
                List.of(
                        new UpdateColumnRequestDTO(doneId, "Finished", null),
                        new UpdateColumnRequestDTO(todoId, "To do", null),
                        new UpdateColumnRequestDTO(doingId, "Doing", ColumnColorEnum.RED)
                )
        );

        // when
        projectService.updateProject(projectId, requestDTO, UUID.randomUUID());
        entityManager.flush();

        // then
        String description = jdbcTemplate.queryForObject("SELECT description FROM projects WHERE id = ?", String.class, projectId);
        assertThat(description).isEqualTo("new description");
        assertThat(columns()).extracting(column -> column.get("name")).containsExactly("Finished", "To do", "Doing");
        assertThat(columns()).extracting(column -> column.get("position")).containsExactly(0, 100, 200);
        // An existing column keeps its color
        assertThat(columns()).extracting(column -> column.get("color")).containsExactly("GREEN", "GRAY", "BLUE");
    }

    @Test
    public void updateProject_shouldAddColumnsWithChosenOrDefaultColor() {
        // given
        UpdateProjectRequestDTO requestDTO = new UpdateProjectRequestDTO(
                "description",
                List.of(
                        new UpdateColumnRequestDTO(todoId, "To do", null),
                        new UpdateColumnRequestDTO(null, "Review", ColumnColorEnum.PINK),
                        new UpdateColumnRequestDTO(doingId, "In progress", null),
                        new UpdateColumnRequestDTO(doneId, "Done", null),
                        new UpdateColumnRequestDTO(null, "Released", null)
                )
        );

        // when
        projectService.updateProject(projectId, requestDTO, UUID.randomUUID());
        entityManager.flush();

        // then
        assertThat(columns()).extracting(column -> column.get("name")).containsExactly("To do", "Review", "In progress", "Done", "Released");
        assertThat(columns()).extracting(column -> column.get("color")).containsExactly("GRAY", "PINK", "BLUE", "GREEN", "AMBER");
    }

    @Test
    public void updateProject_shouldDeleteColumnWithoutBoardTasks() {
        // given
        createTask(null, TaskStageEnum.BACKLOG);
        UpdateProjectRequestDTO requestDTO = new UpdateProjectRequestDTO(
                "description",
                List.of(
                        new UpdateColumnRequestDTO(todoId, "To do", null),
                        new UpdateColumnRequestDTO(doneId, "Done", null)
                )
        );

        // when
        projectService.updateProject(projectId, requestDTO, UUID.randomUUID());
        entityManager.flush();

        // then
        assertThat(columns()).extracting(column -> column.get("name")).containsExactly("To do", "Done");
    }

    @Test
    public void updateProject_shouldRejectDeletingColumnWithBoardTasks() {
        // given
        createTask(doingId, TaskStageEnum.BOARD);
        UpdateProjectRequestDTO requestDTO = new UpdateProjectRequestDTO(
                "description",
                List.of(
                        new UpdateColumnRequestDTO(todoId, "To do", null),
                        new UpdateColumnRequestDTO(doneId, "Done", null)
                )
        );

        // when, then
        assertThatThrownBy(() -> projectService.updateProject(projectId, requestDTO, UUID.randomUUID()))
                .isInstanceOf(ColumnNotEmptyException.class)
                .hasMessageContaining("In progress");
    }

    @Test
    public void updateProject_shouldRejectColumnFromOtherProject() {
        // given
        UUID otherProjectId = tcHelper.createProject("other project");
        UUID otherColumnId = createColumn(otherProjectId, "Other", 0, ColumnColorEnum.GRAY);
        UpdateProjectRequestDTO requestDTO = new UpdateProjectRequestDTO(
                "description",
                List.of(
                        new UpdateColumnRequestDTO(todoId, "To do", null),
                        new UpdateColumnRequestDTO(otherColumnId, "Other", null)
                )
        );

        // when, then
        assertThatThrownBy(() -> projectService.updateProject(projectId, requestDTO, UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private List<Map<String, Object>> columns() {
        return jdbcTemplate.queryForList("SELECT name, position, color FROM columns WHERE project_id = ? ORDER BY position", projectId);
    }

    private UUID createColumn(String name, int position, ColumnColorEnum color) {
        return createColumn(projectId, name, position, color);
    }

    private UUID createColumn(UUID columnProjectId, String name, int position, ColumnColorEnum color) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO columns (id, name, position, project_id, color, version) VALUES (?, ?, ?, ?, ?, ?)",
                id,
                name,
                position,
                columnProjectId,
                color.name(),
                0
        );
        return id;
    }

    private void createTask(UUID columnId, TaskStageEnum stage) {
        jdbcTemplate.update(
                "INSERT INTO tasks (id, title, project_id, column_id, stage, created_date, version) VALUES (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                "task",
                projectId,
                columnId,
                stage.name(),
                Timestamp.from(Instant.now()),
                0
        );
    }
}
