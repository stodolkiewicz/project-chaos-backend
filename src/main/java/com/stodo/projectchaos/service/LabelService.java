package com.stodo.projectchaos.service;

import com.stodo.projectchaos.model.dto.label.response.LabelMapper;
import com.stodo.projectchaos.model.dto.label.response.LabelResponseDTO;
import com.stodo.projectchaos.model.entity.LabelEntity;
import com.stodo.projectchaos.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;

    @Transactional(readOnly = true)
    public LabelResponseDTO getLabelsByProjectId(UUID projectId) {
        List<LabelEntity> labels = labelRepository.findProjectLabelsByProjectId(projectId);

        return LabelResponseDTO.builder()
                .projectId(projectId)
                .labels(LabelMapper.INSTANCE.toLabelDTOList(labels))
                .build();
    }
} 