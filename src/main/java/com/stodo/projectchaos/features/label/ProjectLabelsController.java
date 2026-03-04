package com.stodo.projectchaos.features.label;

import com.stodo.projectchaos.features.label.dto.response.LabelResponseDTO;
import com.stodo.projectchaos.features.label.dto.mapper.LabelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@RequiredArgsConstructor
public class ProjectLabelsController {
    
    private final LabelService labelService;
    
    @GetMapping("/labels")
    public ResponseEntity<LabelResponseDTO> getLabelsByProjectId(
            @PathVariable UUID projectId) {
        LabelResponseDTO response = LabelMapper.INSTANCE.toLabelResponseDTO(
                labelService.getLabelsByProjectId(projectId)
        );
        return ResponseEntity.ok(response);
    }
} 