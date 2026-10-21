package com.stodo.projectchaos.integration.repository;

import com.stodo.projectchaos.features.taskcomments.CustomTaskCommentsRepository;
import com.stodo.projectchaos.features.taskcomments.TaskCommentsRepository;
import com.stodo.projectchaos.features.taskcomments.TaskCommentsService;
import com.stodo.projectchaos.integration.repository.config.TestContainersBase;
import com.stodo.projectchaos.integration.TestcontainersPostgresConfiguration;
import com.stodo.projectchaos.integration.TcHelper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import({
        TestcontainersPostgresConfiguration.class,
        TcHelper.class,
        TaskCommentsService.class,
        CustomTaskCommentsRepository.class
})
class UnreadTaskCommentsTCTest extends TestContainersBase {

    @Autowired
    TcHelper tcHelper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TaskCommentsService taskCommentsService;

    @Autowired
    TaskCommentsRepository taskCommentsRepository;

    @Autowired
    EntityManager entityManager;

    private UUID projectId;
    private UUID readerId;
    private UUID authorId;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        projectId = tcHelper.createProject("project");
        tcHelper.createUserWithDefaultProject("reader@gmail.com", projectId);
        tcHelper.createUserWithDefaultProject("author@gmail.com", projectId);
        readerId = findUserId("reader@gmail.com");
        authorId = findUserId("author@gmail.com");

        taskId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO tasks (id, title, project_id, created_date, version) VALUES (?, ?, ?, ?, ?)",
                taskId,
                "task",
                projectId,
                Timestamp.from(Instant.now()),
                0
        );
    }

    @Test
    public void countUnreadCommentsByTaskIds_shouldCountOnlyOtherUsersComments_whenTaskWasNeverRead() {
        // given
        Timestamp now = Timestamp.from(Instant.now());
        createComment(authorId, now);
        createComment(readerId, now);

        // when
        Long unreadCount = countUnreadComments();

        // then
        assertThat(unreadCount).isEqualTo(1L);
    }

    @Test
    public void countUnreadCommentsByTaskIds_shouldCountOnlyCommentsCreatedAfterLastRead() {
        // given
        markAsRead();
        Timestamp lastReadAt = findLastReadAt();
        createComment(authorId, shiftBySeconds(lastReadAt, -1));
        createComment(authorId, shiftBySeconds(lastReadAt, 1));

        // when
        Long unreadCount = countUnreadComments();

        // then
        assertThat(unreadCount).isEqualTo(1L);
    }

    @Test
    public void markTaskCommentsAsRead_shouldUpdateExistingRow_whenTaskWasAlreadyRead() {
        // given
        markAsRead();
        Timestamp firstReadAt = findLastReadAt();
        createComment(authorId, shiftBySeconds(firstReadAt, 1));

        // when
        jdbcTemplate.update(
                "UPDATE task_comment_reads SET last_read_at = ?",
                shiftBySeconds(firstReadAt, -10)
        );
        markAsRead();

        // then
        Long rowsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM task_comment_reads", Long.class);
        assertThat(rowsCount).isEqualTo(1L);
        assertThat(findLastReadAt()).isAfter(shiftBySeconds(firstReadAt, -10));
    }

    // The test runs in one transaction, so the pending write must be flushed before JDBC reads it
    private void markAsRead() {
        taskCommentsService.markTaskCommentsAsRead(projectId, taskId, readerId);
        entityManager.flush();
    }

    private UUID findUserId(String email) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?",
                UUID.class,
                email
        );
    }

    private Timestamp findLastReadAt() {
        return jdbcTemplate.queryForObject(
                "SELECT last_read_at FROM task_comment_reads WHERE user_id = ? AND task_id = ?",
                Timestamp.class,
                readerId,
                taskId
        );
    }

    // Comment times are derived from the stored last_read_at, so the test does not depend on the JDBC time zone
    private Timestamp shiftBySeconds(Timestamp timestamp, long seconds) {
        return new Timestamp(timestamp.getTime() + seconds * 1000);
    }

    private void createComment(UUID commentAuthorId, Timestamp createdDate) {
        jdbcTemplate.update(
                "INSERT INTO task_comments (id, task_id, author_id, content, created_date, version) VALUES (?, ?, ?, '{}'::jsonb, ?, ?)",
                UUID.randomUUID(),
                taskId,
                commentAuthorId,
                createdDate,
                0
        );
    }

    // The query omits tasks without unread comments, so a missing row means zero
    private Long countUnreadComments() {
        List<Object[]> rows = taskCommentsRepository.countUnreadCommentsByTaskIds(
                List.of(taskId),
                readerId
        );
        return rows.isEmpty() ? 0L : (Long) rows.get(0)[1];
    }
}
