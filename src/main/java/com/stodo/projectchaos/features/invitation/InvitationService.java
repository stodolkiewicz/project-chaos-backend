package com.stodo.projectchaos.features.invitation;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.invitation.dto.response.InvitationStatus;
import com.stodo.projectchaos.features.invitation.dto.service.CreateInvitation;
import com.stodo.projectchaos.features.invitation.dto.service.Invitation;
import com.stodo.projectchaos.features.invitation.dto.mapper.InvitationEntityMapper;
import com.stodo.projectchaos.features.projectuser.dto.service.AssignUserToProject;
import com.stodo.projectchaos.model.entity.InvitationEntity;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.features.projectuser.ProjectUsersRepository;
import com.stodo.projectchaos.features.project.ProjectRepository;
import com.stodo.projectchaos.features.project.ProjectService;
import com.stodo.projectchaos.features.project.CustomProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public CreateInvitation createInvitation(String invitedEmail, UUID projectId, String role, String invitedByEmail) {
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
            AssignUserToProject assignUserToProject = projectService.assignUserToProjectAndHandleUserDefaultProject(projectId, invitedEmail, ProjectRoleEnum.valueOf(role));
            return new CreateInvitation(
                    projectId,
                    invitedEmail,
                    assignUserToProject.projectRole(),
                    InvitationStatus.ADDED

            );
        } else {
            InvitationEntity invitation = InvitationEntity.builder()
                    .id(UUID.randomUUID())
                    .email(invitedEmail)
                    .role(role)
                    .project(project)
                    .invitedBy(invitedBy)
                    .build();

            InvitationEntity savedInvitation = invitationRepository.save(invitation);

            return new CreateInvitation(
                    savedInvitation.getId(),
                    invitedEmail,
                    role,
                    InvitationStatus.INVITED
            );
        }
    }

    public List<Invitation> getInvitationsByEmail(String email) {
        return invitationRepository.findByEmail(email).stream()
                .map(InvitationEntityMapper.INSTANCE::toInvitation)
                .toList();
    }

    public List<Invitation> getInvitationsByProject(UUID projectId) {
        return invitationRepository.findByProjectId(projectId).stream()
                .map(InvitationEntityMapper.INSTANCE::toInvitation)
                .toList();
    }

    public void deleteInvitation(UUID invitationId, UUID projectId) {
        InvitationEntity invitation = invitationRepository.findByIdAndProjectId(invitationId, projectId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("invitationId", invitationId)
                        .entityType("InvitationEntity")
                        .build());
        
        invitationRepository.delete(invitation);
    }


}