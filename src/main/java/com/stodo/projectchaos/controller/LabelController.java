package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.label.response.LabelResponseDTO;
import com.stodo.projectchaos.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@RequiredArgsConstructor
public class LabelController {
    
    private final LabelService labelService;
    
    @GetMapping("/labels")
    public ResponseEntity<LabelResponseDTO> getLabelsByProjectId(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(labelService.getLabelsByProjectId(projectId));
    }
} 