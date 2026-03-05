package com.stodo.projectchaos.features.invitation;

import com.stodo.projectchaos.features.invitation.dto.request.CreateInvitationRequestDTO;
import com.stodo.projectchaos.features.invitation.dto.response.CreateInvitationResponseDTO;
import com.stodo.projectchaos.features.invitation.dto.response.InvitationResponseDTO;
import com.stodo.projectchaos.features.invitation.dto.mapper.InvitationMapper;
import com.stodo.projectchaos.features.invitation.dto.service.Invitation;
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
        Invitation invitation = invitationService.createInvitation(request.email(), projectId, request.role(), invitedByEmail);
        CreateInvitationResponseDTO responseDTO = InvitationMapper.INSTANCE.toCreateInvitationResponseDTO(invitation);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{projectId}/invitations")
    public ResponseEntity<List<InvitationResponseDTO>> getProjectInvitations(
            @PathVariable UUID projectId) {
        List<Invitation> invitations = invitationService.getInvitationsByProject(projectId);
        List<InvitationResponseDTO> responseDTOs = invitations.stream()
                .map(InvitationMapper.INSTANCE::toInvitationResponseDTO)
                .toList();
        return ResponseEntity.ok(responseDTOs);
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @DeleteMapping("/{projectId}/invitations/{invitationId}")
    public ResponseEntity<Void> deleteInvitation(
            @PathVariable UUID invitationId,
            @PathVariable UUID projectId) {
        invitationService.deleteInvitation(invitationId, projectId);
        return ResponseEntity.noContent().build();
    }
}