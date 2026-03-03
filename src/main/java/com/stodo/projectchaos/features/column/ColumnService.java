package com.stodo.projectchaos.features.column;

import com.stodo.projectchaos.features.column.dto.mapper.ColumnMapper;
import com.stodo.projectchaos.features.column.dto.response.ColumnResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColumnService {

    private final ColumnRepository columnRepository;

    public List<ColumnResponseDTO> findColumnsByProjectId(UUID projectId) {
        return columnRepository.findByProjectIdOrderByPosition(projectId)
                .stream()
                .map(ColumnMapper.INSTANCE::toColumnResponseDTO)
                .toList();
    }
} 