package com.stodo.projectchaos.features.label;

import com.stodo.projectchaos.features.label.dto.response.LabelResponseDTO;
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
        return ResponseEntity.ok(labelService.getLabelsByProjectId(projectId));
    }
} 