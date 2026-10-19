package com.stodo.projectchaos.integration.repository;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.task.CustomTaskRepository;
import com.stodo.projectchaos.features.task.dto.request.LabelDTO;
import com.stodo.projectchaos.features.task.dto.request.UpdateTaskRequestDTO;
import com.stodo.projectchaos.integration.repository.config.TestContainersBase;
import com.stodo.projectchaos.integration.repository.config.TestcontainersPostgresConfiguration;
import com.stodo.projectchaos.integration.repository.helper.TcHelper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({
        TestcontainersPostgresConfiguration.class,
        TcHelper.class,
        CustomTaskRepository.class,
        UpdateTaskTCTest.MetricsConfiguration.class
})
class UpdateTaskTCTest extends TestContainersBase {

    @TestConfiguration
    static class MetricsConfiguration {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @Autowired
    TcHelper tcHelper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CustomTaskRepository customTaskRepository;

    @Autowired
    EntityManager entityManager;

    private UUID projectId;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        projectId = tcHelper.createProject("project");
        tcHelper.createUserWithDefaultProject("assignee@gmail.com", projectId);
        taskId = createTask(projectId);
    }

    @Test
    public void updateTask_shouldUpdateTitleDescriptionAssigneeAndPriority() {
        // given
        UUID priorityId = createPriority();
        UpdateTaskRequestDTO requestDTO = new UpdateTaskRequestDTO(
                "new title",
                "new description",
                "assignee@gmail.com",
                priorityId,
                List.of()
        );

        // when
        updateTask(requestDTO);

        // then
        Map<String, Object> task = jdbcTemplate.queryForMap(
                "SELECT t.title, t.description, t.priority_id, u.email FROM tasks t LEFT JOIN users u ON u.id = t.assignee_id WHERE t.id = ?",
                taskId
        );
        assertThat(task.get("title")).isEqualTo("new title");
        assertThat(task.get("description")).isEqualTo("new description");
        assertThat(task.get("priority_id")).isEqualTo(priorityId);
        assertThat(task.get("email")).isEqualTo("assignee@gmail.com");
    }

    @Test
    public void updateTask_shouldReplaceLabelsAndDeleteUnusedOnes() {
        // given
        updateTask(requestWithLabels(List.of(
                new LabelDTO("bug", "#ff0000"),
                new LabelDTO("ui", "#00ff00")
        )));

        // when
        updateTask(requestWithLabels(List.of(
                new LabelDTO(" UI ", "#0000ff"),
                new LabelDTO("backend", "#000000")
        )));

        // then
        List<String> taskLabels = jdbcTemplate.queryForList(
                "SELECT l.name FROM task_labels tl JOIN labels l ON l.id = tl.label_id WHERE tl.task_id = ? ORDER BY l.name",
                String.class,
                taskId
        );
        List<String> projectLabels = jdbcTemplate.queryForList(
                "SELECT name FROM labels WHERE project_id = ? ORDER BY name",
                String.class,
                projectId
        );
        String uiColor = jdbcTemplate.queryForObject(
                "SELECT color FROM labels WHERE name = 'ui' AND project_id = ?",
                String.class,
                projectId
        );
        assertThat(taskLabels).containsExactly("backend", "ui");
        assertThat(projectLabels).containsExactly("backend", "ui");
        assertThat(uiColor).isEqualTo("#0000ff");
    }

    @Test
    public void updateTask_shouldThrowEntityNotFound_whenTaskBelongsToAnotherProject() {
        // given
        UUID otherProjectId = tcHelper.createProject("other project");
        UUID otherTaskId = createTask(otherProjectId);

        // when / then
        assertThatThrownBy(() -> customTaskRepository.updateTask(projectId, otherTaskId, requestWithLabels(List.of())))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // The test runs in one transaction, so pending writes must be flushed before JDBC reads them
    private void updateTask(UpdateTaskRequestDTO requestDTO) {
        customTaskRepository.updateTask(projectId, taskId, requestDTO);
        entityManager.flush();
        entityManager.clear();
    }

    private UpdateTaskRequestDTO requestWithLabels(List<LabelDTO> labels) {
        return new UpdateTaskRequestDTO(
                "title",
                null,
                null,
                null,
                labels
        );
    }

    private UUID createTask(UUID taskProjectId) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO tasks (id, title, project_id, created_date, version) VALUES (?, ?, ?, ?, ?)",
                id,
                "task",
                taskProjectId,
                Timestamp.from(Instant.now()),
                0
        );
        return id;
    }

    private UUID createPriority() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO task_priorities (id, project_id, priority_value, name, color, version) VALUES (?, ?, ?, ?, ?, ?)",
                id,
                projectId,
                1,
                "High",
                "#ff0000",
                0
        );
        return id;
    }
}
