package com.stodo.projectchaos.features.column;

import com.stodo.projectchaos.features.column.dto.response.ColumnResponseDTO;
import com.stodo.projectchaos.features.column.dto.mapper.ColumnMapper;
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
        List<ColumnResponseDTO> response = columnService.findColumnsByProjectId(projectId)
                .stream()
                .map(ColumnMapper.INSTANCE::toColumnResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }
} 