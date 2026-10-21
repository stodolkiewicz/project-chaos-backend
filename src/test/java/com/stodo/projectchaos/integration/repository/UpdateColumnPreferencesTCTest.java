package com.stodo.projectchaos.integration.repository;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.column.ColumnPreferenceService;
import com.stodo.projectchaos.features.column.dto.service.ColumnPreference;
import com.stodo.projectchaos.features.project.CustomProjectRepository;
import com.stodo.projectchaos.features.project.ProjectService;
import com.stodo.projectchaos.features.project.dto.request.UpdateColumnRequestDTO;
import com.stodo.projectchaos.features.project.dto.request.UpdateProjectRequestDTO;
import com.stodo.projectchaos.integration.repository.config.TestContainersBase;
import com.stodo.projectchaos.integration.TestcontainersPostgresConfiguration;
import com.stodo.projectchaos.integration.TcHelper;
import com.stodo.projectchaos.model.enums.ColumnColorEnum;
import com.stodo.projectchaos.model.enums.ColumnSoundEnum;
import com.stodo.projectchaos.storage.AttachmentManager;
import com.stodo.projectchaos.storage.projectlimit.ProjectLimitService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({
        TestcontainersPostgresConfiguration.class,
        TcHelper.class,
        ColumnPreferenceService.class,
        ProjectService.class
})
class UpdateColumnPreferencesTCTest extends TestContainersBase {

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
    ColumnPreferenceService columnPreferenceService;

    @Autowired
    ProjectService projectService;

    @Autowired
    EntityManager entityManager;

    private UUID projectId;
    private UUID userId;
    private UUID otherUserId;
    private UUID todoId;
    private UUID doingId;
    private UUID doneId;

    @BeforeEach
    void setUp() {
        projectId = tcHelper.createProject("project");
        userId = createUser("user@test.com");
        otherUserId = createUser("other@test.com");
        todoId = createColumn(projectId, "To do", 0);
        doingId = createColumn(projectId, "In progress", 100);
        doneId = createColumn(projectId, "Done", 200);
    }

    @Test
    public void getPreferences_shouldCelebrateOnlyLastColumnByDefault() {
        // when
        List<ColumnPreference> preferences = columnPreferenceService.getPreferences(projectId, userId);

        // then
        assertThat(preferences).containsExactly(
                new ColumnPreference(todoId, ColumnSoundEnum.NONE, false),
                new ColumnPreference(doingId, ColumnSoundEnum.NONE, false),
                new ColumnPreference(doneId, ColumnSoundEnum.RANDOM, true)
        );
    }

    @Test
    public void updatePreferences_shouldNotChangePreferencesOfOtherUser() {
        // when
        columnPreferenceService.updatePreferences(projectId, userId, List.of(
                new ColumnPreference(doingId, ColumnSoundEnum.WOW, true),
                new ColumnPreference(doneId, ColumnSoundEnum.NONE, false)
        ));
        entityManager.flush();

        // then
        assertThat(columnPreferenceService.getPreferences(projectId, userId)).containsExactly(
                new ColumnPreference(todoId, ColumnSoundEnum.NONE, false),
                new ColumnPreference(doingId, ColumnSoundEnum.WOW, true),
                new ColumnPreference(doneId, ColumnSoundEnum.NONE, false)
        );
        assertThat(columnPreferenceService.getPreferences(projectId, otherUserId)).containsExactly(
                new ColumnPreference(todoId, ColumnSoundEnum.NONE, false),
                new ColumnPreference(doingId, ColumnSoundEnum.NONE, false),
                new ColumnPreference(doneId, ColumnSoundEnum.RANDOM, true)
        );
    }

    @Test
    public void updatePreferences_shouldOverwriteSavedPreference() {
        // given
        columnPreferenceService.updatePreferences(projectId, userId, List.of(new ColumnPreference(todoId, ColumnSoundEnum.TADA, true)));
        entityManager.flush();

        // when
        columnPreferenceService.updatePreferences(projectId, userId, List.of(new ColumnPreference(todoId, ColumnSoundEnum.YEAHBOY, false)));
        entityManager.flush();

        // then
        assertThat(columnPreferenceService.getPreferences(projectId, userId))
                .first()
                .isEqualTo(new ColumnPreference(todoId, ColumnSoundEnum.YEAHBOY, false));
        assertThat(preferencesCount(todoId)).isEqualTo(1);
    }

    @Test
    public void updatePreferences_shouldRejectColumnFromOtherProject() {
        // given
        UUID otherProjectId = tcHelper.createProject("other project");
        UUID otherColumnId = createColumn(otherProjectId, "Other", 0);

        // when, then
        assertThatThrownBy(() -> columnPreferenceService.updatePreferences(
                projectId,
                userId,
                List.of(new ColumnPreference(otherColumnId, ColumnSoundEnum.WOW, true))
        )).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void updateProject_shouldDeletePreferencesOfAllUsersForDeletedColumn() {
        // given
        columnPreferenceService.updatePreferences(projectId, userId, List.of(new ColumnPreference(doingId, ColumnSoundEnum.WOW, true)));
        columnPreferenceService.updatePreferences(projectId, otherUserId, List.of(new ColumnPreference(doingId, ColumnSoundEnum.TADA, false)));
        entityManager.flush();
        entityManager.clear();

        // when
        projectService.updateProject(
                projectId,
                new UpdateProjectRequestDTO(
                        "description",
                        List.of(
                                new UpdateColumnRequestDTO(todoId, "To do", null),
                                new UpdateColumnRequestDTO(doneId, "Done", null)
                        )
                ),
                userId
        );
        entityManager.flush();

        // then
        assertThat(preferencesCount(doingId)).isZero();
    }

    private int preferencesCount(UUID columnId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_column_preferences WHERE column_id = ?", Integer.class, columnId);
    }

    private UUID createUser(String email) {
        tcHelper.createUserWithDefaultProject(email, projectId);
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", UUID.class, email);
    }

    private UUID createColumn(UUID columnProjectId, String name, int position) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO columns (id, name, position, project_id, color, version) VALUES (?, ?, ?, ?, ?, ?)",
                id,
                name,
                position,
                columnProjectId,
                ColumnColorEnum.GRAY.name(),
                0
        );
        return id;
    }
}
