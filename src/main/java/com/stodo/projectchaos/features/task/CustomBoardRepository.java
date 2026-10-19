package com.stodo.projectchaos.features.task;

import com.stodo.projectchaos.features.task.dto.request.TasksFilterRequestDTO;
import com.stodo.projectchaos.features.task.dto.service.BoardTask;
import com.stodo.projectchaos.features.task.dto.service.Label;
import com.stodo.projectchaos.model.entity.LabelEntity;
import com.stodo.projectchaos.model.enums.TaskStageEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CustomBoardRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public List<BoardTask> findAllTasks(UUID projectId) {
        // Data for tasks (without labels)
        List<BoardTask> tasks = em.createQuery("""
            SELECT new com.stodo.projectchaos.features.task.dto.service.BoardTask(
                t.id,
                t.title,
                t.description,
                t.positionInColumn,
                new com.stodo.projectchaos.features.task.dto.service.Priority(p.id, p.priorityValue, p.name, p.color),
                new com.stodo.projectchaos.features.task.dto.service.Column(c.id, c.name, c.position),
                new com.stodo.projectchaos.features.task.dto.service.Assignee(a.email),
                null,
                t.createdDate,
                null
            )
            FROM TaskEntity t
            LEFT JOIN t.priority p
            LEFT JOIN t.column c
            LEFT JOIN t.assignee a
            WHERE t.project.id = :projectId
            """, BoardTask.class)
                .setParameter("projectId", projectId)
                .getResultList();

        List<UUID> taskIds = tasks.stream()
                .map(BoardTask::getTaskId)
                .collect(Collectors.toList());

        // Labels for all tasks
        Map<UUID, List<Label>> taskLabelsMap = em.createQuery("""
            SELECT tl.task.id as taskId, l 
            FROM TaskLabelsEntity tl 
            JOIN tl.label l 
            WHERE tl.task.id IN :taskIds
            """, Tuple.class)
                .setParameter("taskIds", taskIds)
                .getResultStream()
                .collect(Collectors.groupingBy(
                        // generates key for map
                        tuple -> (UUID) tuple.get("taskId"),
                        // generates value for map - list of labels
                        Collectors.mapping(
                                tuple -> {
                                    LabelEntity e = (LabelEntity) tuple.get(1);
                                    return new Label(e.getId(), e.getName(), e.getColor());
                                },
                                Collectors.toList()
                        )
                ));

        // Assign labels to tasks
        tasks.forEach(task ->
                task.setLabels(taskLabelsMap.getOrDefault(task.getTaskId(), new ArrayList<>()))
        );

        tasks.sort(Comparator.comparing(BoardTask::getCreatedDate));

        return tasks;
    }


    @Transactional
    public List<BoardTask> findSortedBoardTasksByPositionInColumn(UUID projectId) {
        // Data for tasks (without labels)
        List<BoardTask> tasks = em.createQuery("""
        SELECT new com.stodo.projectchaos.features.task.dto.service.BoardTask(
            t.id,
            t.title,
            t.description,
            t.positionInColumn,
            new com.stodo.projectchaos.features.task.dto.service.Priority(p.id, p.priorityValue, p.name, p.color),
            new com.stodo.projectchaos.features.task.dto.service.Column(c.id, c.name, c.position),
            new com.stodo.projectchaos.features.task.dto.service.Assignee(a.email),
            null,
            t.createdDate,
            null
        )
        FROM TaskEntity t
        LEFT JOIN t.priority p
        LEFT JOIN t.column c
        LEFT JOIN t.assignee a
        WHERE c.project.id = :projectId
        AND t.positionInColumn IS NOT NULL
        """, BoardTask.class)
                .setParameter("projectId", projectId)
                .getResultList();

        List<UUID> taskIds = tasks.stream()
                .map(BoardTask::getTaskId)
                .collect(Collectors.toList());

        // Labels for all tasks
        Map<UUID, List<Label>> taskLabelsMap = em.createQuery("""
        SELECT tl.task.id as taskId, l 
        FROM TaskLabelsEntity tl 
        JOIN tl.label l 
        WHERE tl.task.id IN :taskIds
        """, Tuple.class)
                .setParameter("taskIds", taskIds)
                .getResultStream()
                .collect(Collectors.groupingBy(
                        // generates key for map
                        tuple -> (UUID) tuple.get("taskId"),
                        // generates value for map - list of labels
                        Collectors.mapping(
                                tuple -> {
                                    LabelEntity e = (LabelEntity) tuple.get(1);
                                    return new Label(e.getId(), e.getName(), e.getColor());
                                },
                                Collectors.toList()
                        )
                ));

        // Assign labels to tasks
        tasks.forEach(task ->
                task.setLabels(taskLabelsMap.getOrDefault(task.getTaskId(), new ArrayList<>()))
        );

        tasks.sort(
                Comparator
                        .comparing(BoardTask::getPositionInColumn)
                        .thenComparing(task -> task.getColumn().position())
        );

        return tasks;
    }

    @Transactional
    public List<BoardTask> findTasksByStage(UUID projectId, TaskStageEnum stage) {
        List<BoardTask> tasks = em.createQuery("""
            SELECT new com.stodo.projectchaos.features.task.dto.service.BoardTask(
                t.id,
                t.title,
                t.description,
                t.positionInColumn,
                new com.stodo.projectchaos.features.task.dto.service.Priority(p.id, p.priorityValue, p.name, p.color),
                new com.stodo.projectchaos.features.task.dto.service.Column(c.id, c.name, c.position),
                new com.stodo.projectchaos.features.task.dto.service.Assignee(a.email),
                null,
                t.createdDate,
                null
            )
            FROM TaskEntity t
            LEFT JOIN t.priority p
            LEFT JOIN t.column c
            LEFT JOIN t.assignee a
            WHERE t.project.id = :projectId AND t.stage = :stage
            """, BoardTask.class)
                .setParameter("projectId", projectId)
                .setParameter("stage", stage)
                .getResultList();

        List<UUID> taskIds = tasks.stream()
                .map(BoardTask::getTaskId)
                .collect(Collectors.toList());

        if (taskIds.isEmpty()) {
            return new ArrayList<>();
        }

        Map<UUID, List<Label>> taskLabelsMap = em.createQuery("""
            SELECT tl.task.id as taskId, l 
            FROM TaskLabelsEntity tl 
            JOIN tl.label l 
            WHERE tl.task.id IN :taskIds
            """, Tuple.class)
                .setParameter("taskIds", taskIds)
                .getResultStream()
                .collect(Collectors.groupingBy(
                        tuple -> (UUID) tuple.get("taskId"),
                        Collectors.mapping(
                                tuple -> {
                                    LabelEntity e = (LabelEntity) tuple.get(1);
                                    return new Label(e.getId(), e.getName(), e.getColor());
                                },
                                Collectors.toList()
                        )
                ));

        tasks.forEach(task ->
                task.setLabels(taskLabelsMap.getOrDefault(task.getTaskId(), new ArrayList<>()))
        );

        tasks.sort(Comparator.comparing(BoardTask::getCreatedDate));

        return tasks;
    }

    // Newest tasks come first; the id keeps the order stable for tasks created at the same moment
    @Transactional
    public Page<BoardTask> findTasksPageByStage(UUID projectId, TaskStageEnum stage, TasksFilterRequestDTO filter, Pageable pageable) {
        String filterClause = buildFilterClause(filter);

        TypedQuery<Long> countQuery = em.createQuery("""
            SELECT COUNT(t)
            FROM TaskEntity t
            LEFT JOIN t.priority p
            LEFT JOIN t.assignee a
            WHERE t.project.id = :projectId AND t.stage = :stage
            """ + filterClause, Long.class);
        setFilterParameters(countQuery, filter);
        Long totalElements = countQuery
                .setParameter("projectId", projectId)
                .setParameter("stage", stage)
                .getSingleResult();

        if (totalElements == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        TypedQuery<BoardTask> tasksQuery = em.createQuery("""
            SELECT new com.stodo.projectchaos.features.task.dto.service.BoardTask(
                t.id,
                t.title,
                t.description,
                t.positionInColumn,
                new com.stodo.projectchaos.features.task.dto.service.Priority(p.id, p.priorityValue, p.name, p.color),
                new com.stodo.projectchaos.features.task.dto.service.Column(c.id, c.name, c.position),
                new com.stodo.projectchaos.features.task.dto.service.Assignee(a.email),
                null,
                t.createdDate,
                null
            )
            FROM TaskEntity t
            LEFT JOIN t.priority p
            LEFT JOIN t.column c
            LEFT JOIN t.assignee a
            WHERE t.project.id = :projectId AND t.stage = :stage
            """ + filterClause + """
             ORDER BY t.createdDate DESC, t.id
            """, BoardTask.class);
        setFilterParameters(tasksQuery, filter);
        List<BoardTask> tasks = tasksQuery
                .setParameter("projectId", projectId)
                .setParameter("stage", stage)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<UUID> taskIds = tasks.stream()
                .map(BoardTask::getTaskId)
                .toList();

        if (!taskIds.isEmpty()) {
            Map<UUID, List<Label>> taskLabelsMap = em.createQuery("""
                SELECT tl.task.id as taskId, l
                FROM TaskLabelsEntity tl
                JOIN tl.label l
                WHERE tl.task.id IN :taskIds
                """, Tuple.class)
                    .setParameter("taskIds", taskIds)
                    .getResultStream()
                    .collect(Collectors.groupingBy(
                            tuple -> (UUID) tuple.get("taskId"),
                            Collectors.mapping(
                                    tuple -> {
                                        LabelEntity e = (LabelEntity) tuple.get(1);
                                        return new Label(e.getId(), e.getName(), e.getColor());
                                    },
                                    Collectors.toList()
                            )
                    ));

            tasks.forEach(task ->
                    task.setLabels(taskLabelsMap.getOrDefault(task.getTaskId(), new ArrayList<>()))
            );
        }

        return new PageImpl<>(tasks, pageable, totalElements);
    }

    // Uses the aliases p (priority) and a (assignee) of the calling query
    private String buildFilterClause(TasksFilterRequestDTO filter) {
        StringBuilder clause = new StringBuilder();

        List<String> priorityConditions = new ArrayList<>();
        if (!filter.priorityIds().isEmpty()) {
            priorityConditions.add("p.id IN :priorityIds");
        }
        if (filter.withoutPriority()) {
            priorityConditions.add("p.id IS NULL");
        }
        appendAlternatives(clause, priorityConditions);

        List<String> labelConditions = new ArrayList<>();
        if (!filter.labelIds().isEmpty()) {
            labelConditions.add("EXISTS (SELECT 1 FROM TaskLabelsEntity tl WHERE tl.task = t AND tl.label.id IN :labelIds)");
        }
        if (filter.withoutLabel()) {
            labelConditions.add("NOT EXISTS (SELECT 1 FROM TaskLabelsEntity tl WHERE tl.task = t)");
        }
        appendAlternatives(clause, labelConditions);

        List<String> assigneeConditions = new ArrayList<>();
        if (!filter.assigneeIds().isEmpty()) {
            assigneeConditions.add("a.id IN :assigneeIds");
        }
        if (filter.unassigned()) {
            assigneeConditions.add("a.id IS NULL");
        }
        appendAlternatives(clause, assigneeConditions);

        return clause.toString();
    }

    private void appendAlternatives(StringBuilder clause, List<String> conditions) {
        if (!conditions.isEmpty()) {
            clause.append(" AND (").append(String.join(" OR ", conditions)).append(")");
        }
    }

    private void setFilterParameters(TypedQuery<?> query, TasksFilterRequestDTO filter) {
        if (!filter.priorityIds().isEmpty()) {
            query.setParameter("priorityIds", filter.priorityIds());
        }
        if (!filter.labelIds().isEmpty()) {
            query.setParameter("labelIds", filter.labelIds());
        }
        if (!filter.assigneeIds().isEmpty()) {
            query.setParameter("assigneeIds", filter.assigneeIds());
        }
    }

}
