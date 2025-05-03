package com.stodo.projectchaos.service;

import com.stodo.projectchaos.model.dto.column.response.ColumnMapper;
import com.stodo.projectchaos.model.dto.column.response.ColumnResponseDTO;
import com.stodo.projectchaos.repository.ColumnRepository;
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