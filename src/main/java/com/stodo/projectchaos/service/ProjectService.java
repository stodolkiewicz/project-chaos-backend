package com.stodo.projectchaos.service;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.project.byid.response.ProjectMapper;
import com.stodo.projectchaos.model.dto.project.byid.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.create.request.CreateProjectRequestDTO;
import com.stodo.projectchaos.model.dto.project.create.response.CreateProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.query.UserProjectQueryResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.response.DeleteProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.response.UserProjectsResponseDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.request.AssignUserToProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.response.AssignUserToProjectResponseDTO;
import com.stodo.projectchaos.model.dto.user.changerole.request.ChangeUserRoleRequestDTO;
import com.stodo.projectchaos.model.dto.user.changerole.response.ChangeUserRoleResponseDTO;
import com.stodo.projectchaos.model.dto.user.unassign.request.UnassignUserFromProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.projectusers.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.model.dto.user.projectusers.ProjectUserMapper;
import com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.model.entity.*;
import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import com.stodo.projectchaos.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class ProjectService {

    private final CustomProjectRepository customProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUsersRepository projectUsersRepository;
    private final ColumnRepository columnRepository;
    private final TaskPriorityRepository taskPriorityRepository;
    private final AttachmentRepository attachmentRepository;
    private final TaskCommentsRepository taskCommentsRepository;
    private final TaskLabelsRepository taskLabelsRepository;
    private final ProjectBacklogRepository projectBacklogRepository;
    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;

    public ProjectService(CustomProjectRepository customProjectRepository, ProjectRepository projectRepository, UserRepository userRepository, ProjectUsersRepository projectUsersRepository, ColumnRepository columnRepository, TaskPriorityRepository taskPriorityRepository, AttachmentRepository attachmentRepository, TaskCommentsRepository taskCommentsRepository, TaskLabelsRepository taskLabelsRepository, ProjectBacklogRepository projectBacklogRepository, TaskRepository taskRepository, LabelRepository labelRepository) {
        this.customProjectRepository = customProjectRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectUsersRepository = projectUsersRepository;
        this.columnRepository = columnRepository;
        this.taskPriorityRepository = taskPriorityRepository;
        this.attachmentRepository = attachmentRepository;
        this.taskCommentsRepository = taskCommentsRepository;
        this.taskLabelsRepository = taskLabelsRepository;
        this.projectBacklogRepository = projectBacklogRepository;
        this.taskRepository = taskRepository;
        this.labelRepository = labelRepository;
    }

    public DeleteProjectResponseDTO hardDeleteProject(UUID projectId) {
        // todo: delete files from GCS (when it is actually implemented to store them there)
        // todo: notify project members via email
        
        // 1. Handle default_project_id reassignment BEFORE deleting anything
        handleUsersDefaultProjectId(projectId);

        // Get file URLs before deleting attachments (for future GCS cleanup)
        List<String> fileUrls = attachmentRepository.findFilePathsByProjectId(projectId);
        // TODO: Delete physical files from GCS using fileUrls list

        // Level 1: Delete lowest level dependencies
        attachmentRepository.deleteByProjectId(projectId);
        taskCommentsRepository.deleteByProjectId(projectId);
        taskLabelsRepository.deleteByProjectId(projectId);
        projectBacklogRepository.deleteByProjectId(projectId);
        // --------------------------------
        // Level 2: Delete tasks (must be after level 1)
        taskRepository.deleteByProjectId(projectId);

        // Level 3: Delete everything that depends directly on project
        columnRepository.deleteByProjectId(projectId);
        projectUsersRepository.deleteByProjectId(projectId);
        labelRepository.deleteByProjectId(projectId);
        taskPriorityRepository.deleteByProjectId(projectId);

        // -----------------------------------------
        // Level 4: Delete project (final step)
        projectRepository.deleteById(projectId);
        
        return new DeleteProjectResponseDTO(projectId);
    }

    private void handleUsersDefaultProjectId(UUID projectId) {
        // Batch update default projects for users who have alternatives
        userRepository.batchUpdateDefaultProjectsForUsersWithAlternatives(projectId);

        // Clear default project for users without alternatives
        userRepository.clearDefaultProjectForProject(projectId);
    }

    public CreateProjectResponseDTO createProject(CreateProjectRequestDTO createProjectRequestDTO, String email) {
        ProjectEntity savedProjectEntity = saveProject(createProjectRequestDTO);
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("email", email)
                        .entityType("UserEntity")
                        .build());

        setDefaultProjectIdForUser(userEntity, savedProjectEntity);

        addProjectAdmin(email, savedProjectEntity, userEntity);
        saveColumns(createProjectRequestDTO, savedProjectEntity);

        saveDefaultTaskPriorities(savedProjectEntity);

        return new CreateProjectResponseDTO(savedProjectEntity.getId(), savedProjectEntity.getName());
    }

    public Optional<UUID> findDefaultProjectIdByEmail(String email) {
        return userRepository.findDefaultProjectIdByEmail(email);
    }

    private void saveDefaultTaskPriorities(ProjectEntity savedProjectEntity) {
        TaskPriorityEntity taskPriorityEntityLow = new TaskPriorityEntity();
        taskPriorityEntityLow.setProject(savedProjectEntity);
        taskPriorityEntityLow.setName("Low");
        taskPriorityEntityLow.setPriorityValue((short) 1);
        taskPriorityEntityLow.setColor("#A3BE8C");
        taskPriorityRepository.save(taskPriorityEntityLow);

        TaskPriorityEntity taskPriorityEntityMedium = new TaskPriorityEntity();
        taskPriorityEntityMedium.setProject(savedProjectEntity);
        taskPriorityEntityMedium.setName("Medium");
        taskPriorityEntityMedium.setPriorityValue((short) 3);
        taskPriorityEntityMedium.setColor("#EBCB8B");
        taskPriorityRepository.save(taskPriorityEntityMedium);

        TaskPriorityEntity taskPriorityEntityHigh = new TaskPriorityEntity();
        taskPriorityEntityHigh.setProject(savedProjectEntity);
        taskPriorityEntityHigh.setName("High");
        taskPriorityEntityHigh.setPriorityValue((short) 5);
        taskPriorityEntityHigh.setColor("#BF616A");
        taskPriorityRepository.save(taskPriorityEntityHigh);
    }

    private ProjectEntity saveProject(CreateProjectRequestDTO createProjectRequestDTO) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(createProjectRequestDTO.name());
        projectEntity.setDescription(createProjectRequestDTO.description());
        ProjectEntity savedProjectEntity = projectRepository.saveAndFlush(projectEntity);

        return savedProjectEntity;
    }

    private void addProjectAdmin(String email, ProjectEntity savedProjectEntity, UserEntity userEntity) {
        ProjectUsersEntity projectUsersEntity = new ProjectUsersEntity();
        projectUsersEntity.setId(new ProjectUserId(savedProjectEntity.getId(), userEntity.getId()));
        projectUsersEntity.setProjectRole(ProjectRoleEnum.ADMIN);
        projectUsersEntity.setProject(savedProjectEntity);
        projectUsersEntity.setUser(userEntity);

        projectUsersRepository.save(projectUsersEntity);
    }

    private void saveColumns(CreateProjectRequestDTO createProjectRequestDTO, ProjectEntity savedProjectEntity) {
        List<ColumnEntity> columnEntities = new ArrayList<>();
        List<String> columnNames = createProjectRequestDTO.columns();
        for (short i = 0; i < columnNames.size(); i++) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setProject(savedProjectEntity);
            columnEntity.setPosition((short)(i*100));
            columnEntity.setName(columnNames.get(i));

            columnEntities.add(columnEntity);
        }
        columnRepository.saveAll(columnEntities);
    }

    private void setDefaultProjectIdForUser(UserEntity userEntity, ProjectEntity savedProjectEntity) {
            userEntity.setProject(savedProjectEntity);
            userRepository.save(userEntity);
    }

    public ProjectResponseDTO findProjectById(UUID projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectMapper.INSTANCE::toProjectResponseDTO)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("projectId", projectId)
                        .entityType("ProjectEntity")
                        .build());
    }

    public UserProjectsResponseDTO findProjectsByUserEmail(String email) {

        CompletableFuture<List<UserProjectQueryResponseDTO>> userProjectsCF = CompletableFuture.supplyAsync(
                () -> customProjectRepository.findProjectsByUserEmail(email));

        CompletableFuture<Optional<UUID>> defaultUserProjectCF = CompletableFuture.supplyAsync(
                () -> userRepository.findDefaultProjectIdByEmail(email)
        );

        CompletableFuture<Void> cfs = CompletableFuture.allOf(userProjectsCF, defaultUserProjectCF);
        cfs.join();

        List<UserProjectQueryResponseDTO> projects = userProjectsCF.join();
        Optional<UUID> defaultProjectId = defaultUserProjectCF.join();

        moveDefaultProjectToFront(defaultProjectId, projects);

        return new UserProjectsResponseDTO(projects);
    }

    // todo: take a look at it
    private static void moveDefaultProjectToFront(Optional<UUID> defaultProjectId, List<UserProjectQueryResponseDTO> projects) {
        defaultProjectId.ifPresent(id -> {
            Optional<UserProjectQueryResponseDTO> defaultProject = projects.stream()
                    .filter(project -> project.projectId().equals(id))
                    .findFirst();

            List<UserProjectQueryResponseDTO> mutableProjects = new ArrayList<>(projects);
            defaultProject.ifPresent(project -> {
                mutableProjects.remove(project);
                mutableProjects.addFirst(project);

            });
        });
    }

    public boolean isUserAdminInProject(String email, UUID projectId) {
       return customProjectRepository.isUserAdminInProject(email, projectId);
    }

    public boolean hasAtLeastMemberRole(String email, UUID projectId) {
        return customProjectRepository.hasAtLeastMemberRole(email, projectId);
    }

    public ProjectUsersResponseDTO findProjectUsersByProjectId(UUID projectId) {
        List<ProjectUserQueryResponseDTO> users = userRepository.findProjectUsersByProjectId(projectId);
        return ProjectUserMapper.INSTANCE.toProjectUsersResponseDTO(users);
    }

    public AssignUserToProjectResponseDTO assignUserToProject(UUID projectId, AssignUserToProjectRequestDTO assignUserRequest) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("projectId", projectId)
                        .entityType("ProjectEntity")
                        .build());

        UserEntity userToAssign = userRepository.findByEmail(assignUserRequest.userEmail())
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("email", assignUserRequest.userEmail())
                        .entityType("UserEntity")
                        .build());

        // if user does not have defaultProject, assign it to him
        if(userToAssign.getProject() == null) {
            userToAssign.setProject(project);
            userRepository.save(userToAssign);
        }

        // Create or update project user relation
        ProjectUsersEntity projectUsersEntity = new ProjectUsersEntity();
        projectUsersEntity.setId(new ProjectUserId(projectId, userToAssign.getId()));
        projectUsersEntity.setProject(project);
        projectUsersEntity.setUser(userToAssign);
        
        // if projectUsers entity already exists, only role may have changed
        projectUsersEntity.setProjectRole(assignUserRequest.projectRole());
        projectUsersRepository.save(projectUsersEntity);

        return new AssignUserToProjectResponseDTO(
                projectId,
                userToAssign.getId(),
                assignUserRequest.projectRole().getRole()
        );
    }

    public void removeUserFromProject(UUID projectId, UnassignUserFromProjectRequestDTO unassignRequest) {
        // Find user first by email
        UserEntity user = userRepository.findByEmail(unassignRequest.userEmail())
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("email", unassignRequest.userEmail())
                        .entityType("UserEntity")
                        .build());

        // Check if user exists in project
        ProjectUsersEntity projectUser = projectUsersRepository
                .findById(new ProjectUserId(projectId, user.getId()))
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("userEmail", unassignRequest.userEmail())
                        .entityType("ProjectUsersEntity")
                        .build());

        // Check if user is admin and if removing would leave project without admins
        if (projectUser.getProjectRole() == ProjectRoleEnum.ADMIN) {
            long adminCount = projectUsersRepository.countByProjectIdAndProjectRole(projectId, ProjectRoleEnum.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot remove last admin from project. Please assign another admin first.");
            }
        }

        // Remove user from project
        projectUsersRepository.delete(projectUser);

        // If this was user's default project, clear it
        if (user.getProject() != null && user.getProject().getId().equals(projectId)) {
            user.setProject(null);
            userRepository.save(user);
        }
    }

    public ChangeUserRoleResponseDTO changeUserRole(UUID projectId, UUID userId, ChangeUserRoleRequestDTO changeRoleRequest) {
        // Find user first by id
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("id", userId)
                        .entityType("UserEntity")
                        .build());

        // Check if user exists in project
        ProjectUsersEntity projectUser = projectUsersRepository
                .findById(new ProjectUserId(projectId, user.getId()))
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("userId", userId)
                        .entityType("ProjectUsersEntity")
                        .build());

        ProjectRoleEnum currentRole = projectUser.getProjectRole();
        ProjectRoleEnum newRole = changeRoleRequest.projectRole();

        // Check if demoting last admin
        if (currentRole == ProjectRoleEnum.ADMIN && newRole != ProjectRoleEnum.ADMIN) {
            long adminCount = projectUsersRepository.countByProjectIdAndProjectRole(projectId, ProjectRoleEnum.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot demote last admin. Please assign another admin first.");
            }
        }

        // Update user role
        projectUser.setProjectRole(newRole);
        projectUsersRepository.save(projectUser);

        return new ChangeUserRoleResponseDTO(
                projectId,
                userId,
                newRole.getRole()
        );
    }


}
