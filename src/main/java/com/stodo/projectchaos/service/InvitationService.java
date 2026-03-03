package com.stodo.projectchaos.service;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.invitation.create.response.CreateInvitationMapper;
import com.stodo.projectchaos.model.dto.invitation.create.response.CreateInvitationResponseDTO;
import com.stodo.projectchaos.model.dto.invitation.list.response.InvitationMapper;
import com.stodo.projectchaos.model.dto.invitation.list.response.InvitationResponseDTO;
import com.stodo.projectchaos.model.entity.InvitationEntity;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.ProjectUsersEntity;
import com.stodo.projectchaos.model.entity.ProjectUserId;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import com.stodo.projectchaos.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@Transactional
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUsersRepository projectUsersRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final CustomProjectRepository customProjectRepository;

    public InvitationService(InvitationRepository invitationRepository,
                             ProjectRepository projectRepository,
                             ProjectUsersRepository projectUsersRepository,
                             UserRepository userRepository,
                             ProjectService projectService, CustomProjectRepository customProjectRepository) {
        this.invitationRepository = invitationRepository;
        this.projectRepository = projectRepository;
        this.projectUsersRepository = projectUsersRepository;
        this.userRepository = userRepository;
        this.projectService = projectService;
        this.customProjectRepository = customProjectRepository;
    }

    public CreateInvitationResponseDTO createInvitation(String invitedEmail, UUID projectId, String role, String invitedByEmail) {
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

        if(customProjectRepository.ifUserIsInProject(invitedEmail, projectId)) {
            throw new IllegalStateException("The user is already a member of this project");
        }
        if (invitationRepository.existsByEmailAndProjectId(invitedEmail, projectId)) {
            throw new IllegalStateException("Invitation already exists for this email and project");
        }

        boolean invitedUserExists = userRepository.existsByEmail(invitedEmail);
        
        if (invitedUserExists) {
            projectService.assignUserToProject(projectId, invitedEmail, ProjectRoleEnum.valueOf(role));
        }

        // user exists/not exists - always create invitation
        InvitationEntity invitation = InvitationEntity.builder()
                .id(UUID.randomUUID())
                .email(invitedEmail)
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