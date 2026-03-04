package com.stodo.projectchaos.features.label;

import com.stodo.projectchaos.features.label.dto.service.Labels;
import com.stodo.projectchaos.features.label.dto.mapper.LabelEntityMapper;
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
    public Labels getLabelsByProjectId(UUID projectId) {
        return LabelEntityMapper.INSTANCE.toLabels(
                labelRepository.findProjectLabelsByProjectId(projectId),
                projectId
        );
    }
} 