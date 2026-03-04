package com.stodo.projectchaos.features.column;

import com.stodo.projectchaos.features.column.dto.service.Column;
import com.stodo.projectchaos.features.column.dto.mapper.ColumnEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColumnService {

    private final ColumnRepository columnRepository;

    public List<Column> findColumnsByProjectId(UUID projectId) {
        return columnRepository.findByProjectIdOrderByPosition(projectId)
                .stream()
                .map(ColumnEntityMapper.INSTANCE::toColumn)
                .toList();
    }
} 