package com.stodo.projectchaos.integration.repository;

import com.stodo.projectchaos.features.task.CustomBoardRepository;
import com.stodo.projectchaos.features.task.dto.request.TasksFilterRequestDTO;
import com.stodo.projectchaos.features.task.dto.service.BoardTask;
import com.stodo.projectchaos.integration.repository.config.TestContainersBase;
import com.stodo.projectchaos.integration.TestcontainersPostgresConfiguration;
import com.stodo.projectchaos.integration.TcHelper;
import com.stodo.projectchaos.model.enums.TaskStageEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import({
        TestcontainersPostgresConfiguration.class,
        TcHelper.class,
        CustomBoardRepository.class
})
class TasksPageTCTest extends TestContainersBase {

    @Autowired
    TcHelper tcHelper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CustomBoardRepository customBoardRepository;

    private UUID projectId;

    @BeforeEach
    void setUp() {
        projectId = tcHelper.createProject("project");
    }

    @Test
    public void findTasksPageByStage_shouldReturnNewestTasksFirst() {
        // given
        Instant now = Instant.now();
        createTask(projectId, "oldest", TaskStageEnum.BACKLOG, now.minus(3, ChronoUnit.HOURS));
        createTask(projectId, "middle", TaskStageEnum.BACKLOG, now.minus(2, ChronoUnit.HOURS));
        createTask(projectId, "newest", TaskStageEnum.BACKLOG, now.minus(1, ChronoUnit.HOURS));

        // when
        Page<BoardTask> firstPage = customBoardRepository.findTasksPageByStage(projectId, TaskStageEnum.BACKLOG, TasksFilterRequestDTO.none(), PageRequest.of(0, 2));
        Page<BoardTask> secondPage = customBoardRepository.findTasksPageByStage(projectId, TaskStageEnum.BACKLOG, TasksFilterRequestDTO.none(), PageRequest.of(1, 2));

        // then
        assertThat(firstPage.getContent()).extracting(BoardTask::getTitle).containsExactly("newest", "middle");
        assertThat(secondPage.getContent()).extracting(BoardTask::getTitle).containsExactly("oldest");
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    public void findTasksPageByStage_shouldSkipTasksFromOtherStagesAndProjects() {
        // given
        UUID otherProjectId = tcHelper.createProject("other project");
        Instant now = Instant.now();
        createTask(projectId, "archived", TaskStageEnum.ARCHIVED, now);
        createTask(projectId, "backlog", TaskStageEnum.BACKLOG, now);
        createTask(otherProjectId, "other archived", TaskStageEnum.ARCHIVED, now);

        // when
        Page<BoardTask> page = customBoardRepository.findTasksPageByStage(projectId, TaskStageEnum.ARCHIVED, TasksFilterRequestDTO.none(), PageRequest.of(0, 10));

        // then
        assertThat(page.getContent()).extracting(BoardTask::getTitle).containsExactly("archived");
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void findTasksPageByStage_shouldMatchAnyValueWithinFilterAndAllFilters() {
        // given
        UUID highId = createPriority("High");
        UUID lowId = createPriority("Low");
        UUID bugId = createLabel("bug");
        UUID uiId = createLabel("ui");
        tcHelper.createUserWithDefaultProject("assignee@gmail.com", projectId);
        UUID assigneeId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = 'assignee@gmail.com'", UUID.class);

        UUID highBug = createTask(projectId, "high bug", TaskStageEnum.BACKLOG, Instant.now());
        setPriorityAndAssignee(highBug, highId, assigneeId);
        addLabel(highBug, bugId);
        UUID lowUi = createTask(projectId, "low ui", TaskStageEnum.BACKLOG, Instant.now());
        setPriorityAndAssignee(lowUi, lowId, null);
        addLabel(lowUi, uiId);
        createTask(projectId, "bare", TaskStageEnum.BACKLOG, Instant.now());

        // when
        List<String> highOrWithoutPriority = titles(new TasksFilterRequestDTO(List.of(highId), true, null, null, null, null));
        List<String> uiOrWithoutLabel = titles(new TasksFilterRequestDTO(null, null, List.of(uiId), true, null, null));
        List<String> assignedToUser = titles(new TasksFilterRequestDTO(null, null, null, null, List.of(assigneeId), null));
        List<String> lowAndUnassigned = titles(new TasksFilterRequestDTO(List.of(lowId), null, null, null, null, true));
        List<String> highAndUi = titles(new TasksFilterRequestDTO(List.of(highId), null, List.of(uiId), null, null, null));

        // then
        assertThat(highOrWithoutPriority).containsExactlyInAnyOrder("high bug", "bare");
        assertThat(uiOrWithoutLabel).containsExactlyInAnyOrder("low ui", "bare");
        assertThat(assignedToUser).containsExactly("high bug");
        assertThat(lowAndUnassigned).containsExactly("low ui");
        assertThat(highAndUi).isEmpty();
    }

    private List<String> titles(TasksFilterRequestDTO filter) {
        Page<BoardTask> page = customBoardRepository.findTasksPageByStage(projectId, TaskStageEnum.BACKLOG, filter, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(page.getContent().size());
        return page.getContent().stream()
                .map(BoardTask::getTitle)
                .toList();
    }

    private UUID createTask(UUID taskProjectId, String title, TaskStageEnum stage, Instant createdDate) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO tasks (id, title, project_id, stage, created_date, version) VALUES (?, ?, ?, ?, ?, ?)",
                id,
                title,
                taskProjectId,
                stage.name(),
                Timestamp.from(createdDate),
                0
        );
        return id;
    }

    private void setPriorityAndAssignee(UUID taskId, UUID priorityId, UUID assigneeId) {
        jdbcTemplate.update(
                "UPDATE tasks SET priority_id = ?, assignee_id = ? WHERE id = ?",
                priorityId,
                assigneeId,
                taskId
        );
    }

    private UUID createPriority(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO task_priorities (id, project_id, priority_value, name, color, version) VALUES (?, ?, ?, ?, ?, ?)",
                id,
                projectId,
                1,
                name,
                "#ff0000",
                0
        );
        return id;
    }

    private UUID createLabel(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO labels (id, name, color, project_id, version) VALUES (?, ?, ?, ?, ?)",
                id,
                name,
                "#00ff00",
                projectId,
                0
        );
        return id;
    }

    private void addLabel(UUID taskId, UUID labelId) {
        jdbcTemplate.update(
                "INSERT INTO task_labels (task_id, label_id, created_date, version) VALUES (?, ?, ?, ?)",
                taskId,
                labelId,
                Timestamp.from(Instant.now()),
                0
        );
    }
}
