package com.stodo.projectchaos.integration.repository;

import com.stodo.projectchaos.features.task.CustomTaskRepository;
import com.stodo.projectchaos.features.task.dto.request.CreateTaskRequestDTO;
import com.stodo.projectchaos.features.task.dto.request.LabelDTO;
import com.stodo.projectchaos.integration.repository.config.TestContainersBase;
import com.stodo.projectchaos.integration.repository.config.TestcontainersPostgresConfiguration;
import com.stodo.projectchaos.integration.repository.helper.TcHelper;
import com.stodo.projectchaos.model.enums.TaskStageEnum;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import({
        TestcontainersPostgresConfiguration.class,
        TcHelper.class,
        CustomTaskRepository.class,
        CreateTaskTCTest.MetricsConfiguration.class
})
class CreateTaskTCTest extends TestContainersBase {

    @TestConfiguration
    static class MetricsConfiguration {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    TcHelper tcHelper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CustomTaskRepository customTaskRepository;

    @Autowired
    EntityManager entityManager;

    private UUID projectId;

    @BeforeEach
    void setUp() {
        projectId = tcHelper.createProject("project");
        tcHelper.createUserWithDefaultProject("assignee@gmail.com", projectId);
    }

    @Test
    public void createTask_shouldSaveTaskWithoutAssignee_whenAssigneeEmailIsNull() {
        // given
        CreateTaskRequestDTO requestDTO = request(null, TaskStageEnum.BACKLOG, List.of());

        // when
        UUID taskId = createTask(requestDTO);

        // then
        assertThat(validator.validate(requestDTO)).isEmpty();
        assertThat(assigneeEmail(taskId)).isNull();
    }

    @Test
    public void createTask_shouldSaveTaskWithoutAssignee_whenAssigneeEmailIsEmpty() {
        // given
        CreateTaskRequestDTO requestDTO = request("", TaskStageEnum.BACKLOG, List.of());

        // when
        UUID taskId = createTask(requestDTO);

        // then
        assertThat(validator.validate(requestDTO)).isEmpty();
        assertThat(assigneeEmail(taskId)).isNull();
    }

    @Test
    public void createTask_shouldSaveAssigneeAndLabels() {
        // given
        CreateTaskRequestDTO requestDTO = request(
                "assignee@gmail.com",
                TaskStageEnum.BACKLOG,
                List.of(new LabelDTO(" Bug ", "#ff0000"))
        );

        // when
        UUID taskId = createTask(requestDTO);

        // then
        List<String> labels = jdbcTemplate.queryForList(
                "SELECT l.name FROM task_labels tl JOIN labels l ON l.id = tl.label_id WHERE tl.task_id = ?",
                String.class,
                taskId
        );
        assertThat(validator.validate(requestDTO)).isEmpty();
        assertThat(assigneeEmail(taskId)).isEqualTo("assignee@gmail.com");
        assertThat(labels).containsExactly("bug");
    }

    @Test
    public void validation_shouldRejectMissingStage() {
        // given
        CreateTaskRequestDTO requestDTO = request(null, null, List.of());

        // when
        List<String> invalidFields = invalidFields(requestDTO);

        // then
        assertThat(invalidFields).containsExactly("stage");
    }

    @Test
    public void validation_shouldRejectLabelWithBlankNameOrColor() {
        // given
        CreateTaskRequestDTO requestDTO = request(
                null,
                TaskStageEnum.BACKLOG,
                List.of(
                        new LabelDTO(" ", "#ff0000"),
                        new LabelDTO("bug", null)
                )
        );

        // when
        List<String> invalidFields = invalidFields(requestDTO);

        // then
        assertThat(invalidFields).containsExactlyInAnyOrder("labels[0].name", "labels[1].color");
    }

    // The test runs in one transaction, so pending writes must be flushed before JDBC reads them
    private UUID createTask(CreateTaskRequestDTO requestDTO) {
        UUID taskId = customTaskRepository.createTask(requestDTO, projectId).id();
        entityManager.flush();
        entityManager.clear();
        return taskId;
    }

    private String assigneeEmail(UUID taskId) {
        Map<String, Object> task = jdbcTemplate.queryForMap(
                "SELECT u.email FROM tasks t LEFT JOIN users u ON u.id = t.assignee_id WHERE t.id = ?",
                taskId
        );
        return (String) task.get("email");
    }

    private List<String> invalidFields(CreateTaskRequestDTO requestDTO) {
        return validator.validate(requestDTO).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .toList();
    }

    private CreateTaskRequestDTO request(String assigneeEmail, TaskStageEnum stage, List<LabelDTO> labels) {
        return new CreateTaskRequestDTO(
                "title",
                null,
                1.0,
                assigneeEmail,
                null,
                null,
                labels,
                stage
        );
    }
}
