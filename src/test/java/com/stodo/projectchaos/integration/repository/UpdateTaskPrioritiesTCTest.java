package com.stodo.projectchaos.integration.repository;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.priority.DuplicatePriorityValueException;
import com.stodo.projectchaos.features.priority.TaskPriorityService;
import com.stodo.projectchaos.features.priority.dto.request.UpdateTaskPrioritiesRequestDTO;
import com.stodo.projectchaos.features.priority.dto.request.UpdateTaskPriorityRequestDTO;
import com.stodo.projectchaos.features.priority.dto.service.TaskPriority;
import com.stodo.projectchaos.integration.repository.config.TestContainersBase;
import com.stodo.projectchaos.integration.repository.config.TestcontainersPostgresConfiguration;
import com.stodo.projectchaos.integration.repository.helper.TcHelper;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@Import({
        TestcontainersPostgresConfiguration.class,
        TcHelper.class,
        TaskPriorityService.class
})
class UpdateTaskPrioritiesTCTest extends TestContainersBase {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    TcHelper tcHelper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TaskPriorityService taskPriorityService;

    @Autowired
    EntityManager entityManager;

    private final UUID userId = UUID.randomUUID();
    private UUID projectId;
    private UUID lowId;
    private UUID highId;

    @BeforeEach
    void setUp() {
        projectId = tcHelper.createProject("project");
        lowId = createPriority(projectId, "Low", 1);
        highId = createPriority(projectId, "High", 5);
    }

    @Test
    public void updateTaskPriorities_shouldChangeNameValueAndColor_andKeepThePriorityOnTasks() {
        // given
        UUID taskId = createTask(lowId);

        // when
        update(List.of(
                new TaskPriority(lowId, (short) 2, " Minor ", "#123456", 0),
                new TaskPriority(highId, (short) 5, "High", "#BF616A", 0)
        ));

        // then
        Map<String, Object> low = jdbcTemplate.queryForMap(
                "SELECT name, priority_value, color FROM task_priorities WHERE id = ?",
                lowId
        );
        assertThat(low.get("name")).isEqualTo("Minor");
        assertThat(((Number) low.get("priority_value")).intValue()).isEqualTo(2);
        assertThat(low.get("color")).isEqualTo("#123456");
        assertThat(taskPriorityId(taskId)).isEqualTo(lowId);
    }

    @Test
    public void updateTaskPriorities_shouldCreatePriorityWithoutId() {
        // when
        update(List.of(
                new TaskPriority(lowId, (short) 1, "Low", "#A3BE8C", 0),
                new TaskPriority(highId, (short) 5, "High", "#BF616A", 0),
                new TaskPriority(null, (short) 3, "Medium", "#EBCB8B", 0)
        ));

        // then
        List<String> names = jdbcTemplate.queryForList(
                "SELECT name FROM task_priorities WHERE project_id = ? ORDER BY priority_value",
                String.class,
                projectId
        );
        assertThat(names).containsExactly("Low", "Medium", "High");
    }

    @Test
    public void updateTaskPriorities_shouldSwapValuesOfTwoPriorities() {
        // when
        update(List.of(
                new TaskPriority(lowId, (short) 5, "Low", "#A3BE8C", 0),
                new TaskPriority(highId, (short) 1, "High", "#BF616A", 0)
        ));
        // The unique constraint is checked at commit, and the test never commits, so it is checked now
        jdbcTemplate.execute("SET CONSTRAINTS uq_task_priorities_project_value IMMEDIATE");

        // then
        List<String> names = jdbcTemplate.queryForList(
                "SELECT name FROM task_priorities WHERE project_id = ? ORDER BY priority_value",
                String.class,
                projectId
        );
        assertThat(names).containsExactly("High", "Low");
    }

    @Test
    public void updateTaskPriorities_shouldRejectTwoPrioritiesWithTheSameValue() {
        // given
        List<TaskPriority> priorities = List.of(
                new TaskPriority(lowId, (short) 5, "Low", "#A3BE8C", 0),
                new TaskPriority(highId, (short) 5, "High", "#BF616A", 0)
        );

        // when / then
        assertThatThrownBy(() -> taskPriorityService.updateTaskPriorities(projectId, userId, priorities))
                .isInstanceOf(DuplicatePriorityValueException.class);
    }

    @Test
    public void updateTaskPriorities_shouldDeletePriorityAndClearItOnTasks() {
        // given
        UUID taskId = createTask(lowId);

        // when
        update(List.of(new TaskPriority(highId, (short) 5, "High", "#BF616A", 0)));

        // then
        Long lowCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM task_priorities WHERE id = ?",
                Long.class,
                lowId
        );
        assertThat(lowCount).isZero();
        assertThat(taskPriorityId(taskId)).isNull();
    }

    @Test
    public void updateTaskPriorities_shouldThrowEntityNotFound_whenPriorityBelongsToAnotherProject() {
        // given
        UUID otherProjectId = tcHelper.createProject("other project");
        UUID otherPriorityId = createPriority(otherProjectId, "Other", 1);
        List<TaskPriority> priorities = List.of(new TaskPriority(otherPriorityId, (short) 1, "Other", "#A3BE8C", 0));

        // when / then
        assertThatThrownBy(() -> taskPriorityService.updateTaskPriorities(projectId, userId, priorities))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void findTaskPrioritiesByProjectId_shouldCountTasksOfEveryPriority() {
        // given
        createTask(lowId);
        createTask(lowId);

        // when
        List<TaskPriority> priorities = taskPriorityService.findTaskPrioritiesByProjectId(projectId);

        // then
        assertThat(priorities)
                .extracting(TaskPriority::name, TaskPriority::taskCount)
                .containsExactly(
                        tuple("Low", 2L),
                        tuple("High", 0L)
                );
    }

    @Test
    public void validation_shouldRejectWrongValueNameColorAndTooManyPriorities() {
        // given
        UpdateTaskPrioritiesRequestDTO wrongFields = new UpdateTaskPrioritiesRequestDTO(List.of(
                new UpdateTaskPriorityRequestDTO(null, " ", (short) 6, "red")
        ));
        UpdateTaskPrioritiesRequestDTO tooMany = new UpdateTaskPrioritiesRequestDTO(
                IntStream.rangeClosed(1, 6)
                        .mapToObj(i -> new UpdateTaskPriorityRequestDTO(null, "P" + i, (short) 1, "#A3BE8C"))
                        .toList()
        );

        // when / then
        assertThat(invalidFields(wrongFields)).containsExactlyInAnyOrder(
                "priorities[0].name",
                "priorities[0].priorityValue",
                "priorities[0].color"
        );
        assertThat(invalidFields(tooMany)).containsExactly("priorities");
    }

    // The test runs in one transaction, so pending writes must be flushed before JDBC reads them
    private void update(List<TaskPriority> priorities) {
        taskPriorityService.updateTaskPriorities(projectId, userId, priorities);
        entityManager.flush();
        entityManager.clear();
    }

    private List<String> invalidFields(UpdateTaskPrioritiesRequestDTO requestDTO) {
        return validator.validate(requestDTO).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .toList();
    }

    private UUID taskPriorityId(UUID taskId) {
        return jdbcTemplate.queryForObject(
                "SELECT priority_id FROM tasks WHERE id = ?",
                UUID.class,
                taskId
        );
    }

    private UUID createPriority(UUID priorityProjectId, String name, int value) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO task_priorities (id, project_id, priority_value, name, color, version) VALUES (?, ?, ?, ?, ?, ?)",
                id,
                priorityProjectId,
                value,
                name,
                "#A3BE8C",
                0
        );
        return id;
    }

    private UUID createTask(UUID priorityId) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO tasks (id, title, project_id, priority_id, stage, created_date, version) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id,
                "task",
                projectId,
                priorityId,
                "BACKLOG",
                Timestamp.from(Instant.now()),
                0
        );
        return id;
    }
}
