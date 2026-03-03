package com.stodo.projectchaos.features.invitation;

import com.stodo.projectchaos.features.invitation.dto.request.CreateInvitationRequestDTO;
import com.stodo.projectchaos.features.invitation.dto.response.CreateInvitationResponseDTO;
import com.stodo.projectchaos.features.invitation.dto.response.InvitationResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @PostMapping("/{projectId}/invitations")
    public ResponseEntity<CreateInvitationResponseDTO> createInvitation(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateInvitationRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String invitedByEmail = userDetails.getUsername();
        CreateInvitationResponseDTO invitation = invitationService.createInvitation(request.email(), projectId, request.role(), invitedByEmail);
        return ResponseEntity.ok(invitation);
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @GetMapping("/{projectId}/invitations")
    public ResponseEntity<List<InvitationResponseDTO>> getProjectInvitations(
            @PathVariable UUID projectId) {
        List<InvitationResponseDTO> invitations = invitationService.getInvitationsByProject(projectId);
        return ResponseEntity.ok(invitations);
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @DeleteMapping("/invitations/{invitationId}")
    public ResponseEntity<Void> deleteInvitation(
            @PathVariable UUID invitationId) {
        invitationService.deleteInvitation(invitationId);
        return ResponseEntity.noContent().build();
    }
}