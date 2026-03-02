package com.stodo.projectchaos.service;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.invitation.create.response.CreateInvitationMapper;
import com.stodo.projectchaos.model.dto.invitation.create.response.CreateInvitationResponseDTO;
import com.stodo.projectchaos.model.dto.invitation.list.response.InvitationMapper;
import com.stodo.projectchaos.model.dto.invitation.list.response.InvitationResponseDTO;
import com.stodo.projectchaos.model.entity.InvitationEntity;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.repository.InvitationRepository;
import com.stodo.projectchaos.repository.ProjectRepository;
import com.stodo.projectchaos.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public InvitationService(InvitationRepository invitationRepository, 
                           ProjectRepository projectRepository, 
                           UserRepository userRepository) {
        this.invitationRepository = invitationRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public CreateInvitationResponseDTO createInvitation(String email, UUID projectId, String role, String invitedByEmail) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("projectId", projectId)
                        .entityType("ProjectEntity")
                        .build());

        UserEntity invitedBy = userRepository.findByEmail(invitedByEmail)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("email", invitedByEmail)
                        .entityType("UserEntity")
                        .build());

        if (invitationRepository.existsByEmailAndProjectId(email, projectId)) {
            throw new IllegalStateException("Invitation already exists for this email and project");
        }

        InvitationEntity invitation = InvitationEntity.builder()
                .email(email)
                .role(role)
                .project(project)
                .invitedBy(invitedBy)
                .build();

        InvitationEntity savedInvitation = invitationRepository.save(invitation);
        
        return CreateInvitationMapper.INSTANCE.toCreateInvitationResponseDTO(savedInvitation);
    }

    public List<InvitationResponseDTO> getInvitationsByEmail(String email) {
        return invitationRepository.findByEmail(email).stream()
                .map(InvitationMapper.INSTANCE::toInvitationResponseDTO)
                .toList();
    }

    public List<InvitationResponseDTO> getInvitationsByProject(UUID projectId) {
        return invitationRepository.findByProjectId(projectId).stream()
                .map(InvitationMapper.INSTANCE::toInvitationResponseDTO)
                .toList();
    }

    public void deleteInvitation(UUID invitationId) {
        InvitationEntity invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("invitationId", invitationId)
                        .entityType("InvitationEntity")
                        .build());
        
        invitationRepository.delete(invitation);
    }

}