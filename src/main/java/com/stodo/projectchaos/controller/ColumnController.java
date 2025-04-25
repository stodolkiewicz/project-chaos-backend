package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.response.ColumnResponseDTO;
import com.stodo.projectchaos.service.ColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ColumnController {

    private final ColumnService columnService;

    @GetMapping("/{projectId}/columns")
    public ResponseEntity<List<ColumnResponseDTO>> getColumnsByProjectId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(columnService.findColumnsByProjectId(projectId));
    }
} 