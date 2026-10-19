package com.stodo.projectchaos.features.column;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.column.dto.mapper.ColumnPreferenceEntityMapper;
import com.stodo.projectchaos.features.column.dto.service.ColumnPreference;
import com.stodo.projectchaos.model.entity.ColumnEntity;
import com.stodo.projectchaos.model.entity.UserColumnPreferenceEntity;
import com.stodo.projectchaos.model.entity.UserColumnPreferenceId;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.enums.ColumnSoundEnum;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColumnPreferenceService {

    private final ColumnRepository columnRepository;
    private final UserColumnPreferenceRepository userColumnPreferenceRepository;
    private final EntityManager entityManager;

    public List<ColumnPreference> getPreferences(UUID projectId, UUID userId) {
        List<ColumnEntity> columns = columnRepository.findByProjectIdOrderByPosition(projectId);
        Map<UUID, UserColumnPreferenceEntity> saved = userColumnPreferenceRepository.findByUserIdAndColumnProjectId(userId, projectId)
                .stream()
                .collect(Collectors.toMap(preference -> preference.getId().getColumn(), Function.identity()));

        return IntStream.range(0, columns.size())
                .mapToObj(i -> {
                    UUID columnId = columns.get(i).getId();
                    UserColumnPreferenceEntity preference = saved.get(columnId);
                    if (preference != null) {
                        return ColumnPreferenceEntityMapper.INSTANCE.toColumnPreference(preference);
                    }
                    return defaultPreference(columnId, i == columns.size() - 1);
                })
                .toList();
    }

    @Transactional
    public void updatePreferences(UUID projectId, UUID userId, List<ColumnPreference> preferences) {
        Map<UUID, ColumnEntity> columns = columnRepository.findByProjectIdOrderByPosition(projectId)
                .stream()
                .collect(Collectors.toMap(ColumnEntity::getId, Function.identity()));

        List<UserColumnPreferenceEntity> entities = preferences.stream()
                .map(preference -> {
                    ColumnEntity column = columns.get(preference.columnId());
                    if (column == null) {
                        throw EntityNotFoundException.builder()
                                .entityType("ColumnEntity")
                                .identifier("columnId", preference.columnId())
                                .identifier("projectId", projectId)
                                .build();
                    }
                    UserColumnPreferenceId id = new UserColumnPreferenceId(userId, column.getId());
                    UserColumnPreferenceEntity entity = userColumnPreferenceRepository.findById(id)
                            .orElseGet(() -> UserColumnPreferenceEntity.builder()
                                    .id(id)
                                    .user(entityManager.getReference(UserEntity.class, userId))
                                    .column(column)
                                    .build());
                    entity.setSound(preference.sound());
                    entity.setConfetti(preference.confetti());
                    return entity;
                })
                .toList();

        userColumnPreferenceRepository.saveAll(entities);
        log.info("Column preferences updated [projectId={}, userId={}, columns={}]", projectId, userId, entities.size());
    }

    // The board celebrates only a task that reaches the last column, unless the user chose otherwise
    private ColumnPreference defaultPreference(UUID columnId, boolean isLastColumn) {
        return isLastColumn
                ? new ColumnPreference(columnId, ColumnSoundEnum.RANDOM, true)
                : new ColumnPreference(columnId, ColumnSoundEnum.NONE, false);
    }
}
