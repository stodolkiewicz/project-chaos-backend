package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.dto.task.board.response.BoardTasksResponseDTO;
import com.stodo.projectchaos.model.dto.task.board.response.LabelDTO;
import com.stodo.projectchaos.model.entity.LabelEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CustomBoardRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public List<BoardTasksResponseDTO> findBoardTasks(UUID projectId) {
        // Data for tasks (without labels)
        List<BoardTasksResponseDTO> tasks = em.createQuery("""
            SELECT new com.stodo.projectchaos.model.dto.task.board.response.BoardTasksResponseDTO(
                t.id,
                t.title,
                t.description,
                t.positionInColumn,
                new com.stodo.projectchaos.model.dto.task.board.response.PriorityDTO(p.id, p.priorityValue, p.name, p.color),
                new com.stodo.projectchaos.model.dto.task.board.response.ColumnDTO(c.id, c.name, c.position),
                new com.stodo.projectchaos.model.dto.task.board.response.AssigneeDTO(a.email),
                null
            )
            FROM TaskEntity t
            LEFT JOIN t.priority p
            JOIN t.column c
            LEFT JOIN t.assignee a
            WHERE c.project.id = :projectId
            """, BoardTasksResponseDTO.class)
                .setParameter("projectId", projectId)
                .getResultList();

        List<UUID> taskIds = tasks.stream()
                .map(BoardTasksResponseDTO::getTaskId)
                .collect(Collectors.toList());

        // Labels for all tasks
        Map<UUID, List<LabelDTO>> taskLabelsMap = em.createQuery("""
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
                                    return new LabelDTO(e.getId(), e.getName(), e.getColor());
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
                        .comparing(BoardTasksResponseDTO::getPositionInColumn)
                        .thenComparing(task -> task.getColumn().getPosition())
        );

        return tasks;
    }

}
