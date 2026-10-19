package com.stodo.projectchaos.features.column;

import com.stodo.projectchaos.features.column.dto.mapper.ColumnPreferenceMapper;
import com.stodo.projectchaos.features.column.dto.request.UpdateColumnPreferenceRequestDTO;
import com.stodo.projectchaos.features.column.dto.response.ColumnPreferenceResponseDTO;
import com.stodo.projectchaos.features.column.dto.response.ColumnResponseDTO;
import com.stodo.projectchaos.features.column.dto.mapper.ColumnMapper;
import com.stodo.projectchaos.security.filter.AppUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ColumnController {

    private final ColumnService columnService;
    private final ColumnPreferenceService columnPreferenceService;

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

    @PreAuthorize("@projectSecurity.hasAtLeastMemberRole(#projectId, authentication)")
    @GetMapping("/{projectId}/columns/preferences")
    public ResponseEntity<List<ColumnPreferenceResponseDTO>> getColumnPreferences(
            @PathVariable UUID projectId,
            Authentication authentication) {
        UUID userId = ((AppUserDetails) authentication.getPrincipal()).getUserId();
        List<ColumnPreferenceResponseDTO> response = columnPreferenceService.getPreferences(projectId, userId)
                .stream()
                .map(ColumnPreferenceMapper.INSTANCE::toColumnPreferenceResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@projectSecurity.hasAtLeastMemberRole(#projectId, authentication)")
    @PutMapping("/{projectId}/columns/preferences")
    public ResponseEntity<Void> updateColumnPreferences(
            @PathVariable UUID projectId,
            @RequestBody List<@Valid UpdateColumnPreferenceRequestDTO> requestDTOs,
            Authentication authentication) {
        UUID userId = ((AppUserDetails) authentication.getPrincipal()).getUserId();
        columnPreferenceService.updatePreferences(
                projectId,
                userId,
                requestDTOs.stream()
                        .map(ColumnPreferenceMapper.INSTANCE::toColumnPreference)
                        .toList()
        );
        return ResponseEntity.noContent().build();
    }
}
