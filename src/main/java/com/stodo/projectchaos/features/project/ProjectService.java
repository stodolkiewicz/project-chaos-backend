package com.stodo.projectchaos.features.project;

import com.stodo.projectchaos.ai.features.conversation.AIConversationRepository;
import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.exception.UserAlreadyInProjectException;
import com.stodo.projectchaos.features.column.ColumnRepository;
import com.stodo.projectchaos.features.label.LabelRepository;
import com.stodo.projectchaos.features.priority.TaskPriorityRepository;
import com.stodo.projectchaos.features.project.dto.mapper.ProjectEntityMapper;
import com.stodo.projectchaos.features.project.dto.query.UserProjectQueryResponseDTO;
import com.stodo.projectchaos.features.project.dto.request.CreateColumnRequestDTO;
import com.stodo.projectchaos.features.project.dto.request.CreateProjectRequestDTO;
import com.stodo.projectchaos.features.project.dto.request.UpdateColumnRequestDTO;
import com.stodo.projectchaos.features.project.dto.request.UpdateProjectRequestDTO;
import com.stodo.projectchaos.features.project.dto.service.Project;
import com.stodo.projectchaos.features.project.dto.service.ProjectDelete;
import com.stodo.projectchaos.features.project.dto.service.UserProjects;
import com.stodo.projectchaos.features.projectuser.ProjectUsersRepository;
import com.stodo.projectchaos.features.projectuser.dto.service.AssignUserToProject;
import com.stodo.projectchaos.features.attachment.AttachmentRepository;
import com.stodo.projectchaos.storage.AttachmentManager;
import com.stodo.projectchaos.features.taskcomments.TaskCommentsRepository;
import com.stodo.projectchaos.features.task.TaskLabelsRepository;
import com.stodo.projectchaos.features.task.TaskRepository;
import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.features.user.dto.request.ChangeUserRoleRequestDTO;
import com.stodo.projectchaos.features.user.dto.response.ChangeUserRoleResponseDTO;
import com.stodo.projectchaos.model.entity.*;
import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import com.stodo.projectchaos.model.enums.TaskStageEnum;
import com.stodo.projectchaos.storage.projectlimit.ProjectLimitService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.stodo.projectchaos.model.enums.ColumnColorEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
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
    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;
    private final AIConversationRepository aiConversationRepository;
    private final ProjectLimitService projectLimitService;
    private final AttachmentManager attachmentManager;

    // Thread pool created by Spring Boot.
    // Unlike the default pool of CompletableFuture, it passes the trace context to the async tasks.
    private final Executor applicationTaskExecutor;

    public ProjectService(
            CustomProjectRepository customProjectRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectUsersRepository projectUsersRepository,
            ColumnRepository columnRepository,
            TaskPriorityRepository taskPriorityRepository,
            AttachmentRepository attachmentRepository,
            TaskCommentsRepository taskCommentsRepository,
            TaskLabelsRepository taskLabelsRepository,
            TaskRepository taskRepository,
            LabelRepository labelRepository,
            AIConversationRepository aiConversationRepository,
            ProjectLimitService projectLimitService,
            AttachmentManager attachmentManager,
            @Qualifier("applicationTaskExecutor") Executor applicationTaskExecutor
    ) {
        this.customProjectRepository = customProjectRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectUsersRepository = projectUsersRepository;
        this.columnRepository = columnRepository;
        this.taskPriorityRepository = taskPriorityRepository;
        this.attachmentRepository = attachmentRepository;
        this.taskCommentsRepository = taskCommentsRepository;
        this.taskLabelsRepository = taskLabelsRepository;
        this.taskRepository = taskRepository;
        this.labelRepository = labelRepository;
        this.aiConversationRepository = aiConversationRepository;
        this.projectLimitService = projectLimitService;
        this.attachmentManager = attachmentManager;
        this.applicationTaskExecutor = applicationTaskExecutor;
    }

    public ProjectDelete hardDeleteProject(UUID projectId) {
        // todo: delete files from GCS (when it is actually implemented to store them there)
        // todo: notify project members via email
        
        // 1. Handle default_project_id reassignment BEFORE deleting anything
        handleUsersDefaultProjectId(projectId);

        // Delete all attachments (files from GCS and database records)
        attachmentManager.deleteProjectFiles(projectId);

        taskRepository.clearPriorityByProjectId(projectId);

        // Level 1: Delete lowest level dependencies
        taskCommentsRepository.deleteByProjectId(projectId);
        taskLabelsRepository.deleteByProjectId(projectId);
        aiConversationRepository.deleteByProjectId(projectId);
        taskPriorityRepository.deleteByProjectId(projectId);

        // --------------------------------
        // Level 2: Delete tasks (must be after level 1)
        taskRepository.deleteByProjectId(projectId);

        // Level 3: Delete everything that depends directly on project
        columnRepository.deleteByProjectId(projectId);
        projectUsersRepository.deleteByProjectId(projectId);
        labelRepository.deleteByProjectId(projectId);

        // -----------------------------------------
        // Level 4: Delete project (final step)
        projectRepository.deleteById(projectId);

        log.info("Project deleted [projectId={}]", projectId);

        return new ProjectDelete(projectId);
    }

    private void handleUsersDefaultProjectId(UUID projectId) {
        // Batch update default projects for users who have alternatives
        userRepository.batchUpdateDefaultProjectsForUsersWithAlternatives(projectId);

        // Clear default project for users without alternatives
        userRepository.clearDefaultProjectForProject(projectId);
    }

    public Project createProject(CreateProjectRequestDTO createProjectRequestDTO, String email) {
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
        
        projectLimitService.createProjectStorageUsage(savedProjectEntity.getId());

        log.info("Project created [projectId={}, userId={}]",
                savedProjectEntity.getId(),
                userEntity.getId());

        return ProjectEntityMapper.INSTANCE.toProject(savedProjectEntity);
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
        List<CreateColumnRequestDTO> columns = createProjectRequestDTO.columns();
        for (short i = 0; i < columns.size(); i++) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setProject(savedProjectEntity);
            columnEntity.setPosition((short)(i*100));
            columnEntity.setName(columns.get(i).name());
            columnEntity.setColor(columns.get(i).color());

            columnEntities.add(columnEntity);
        }
        assignDefaultColors(columnEntities);
        columnRepository.saveAll(columnEntities);
    }

    // Columns without a color get the first palette color that no other column uses; when all are used, the palette repeats
    private void assignDefaultColors(List<ColumnEntity> columnEntities) {
        Set<ColumnColorEnum> usedColors = columnEntities.stream()
                .map(ColumnEntity::getColor)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ColumnColorEnum.class)));
        ColumnColorEnum[] palette = ColumnColorEnum.values();
        int nextRepeatedColor = 0;

        for (ColumnEntity columnEntity : columnEntities) {
            if (columnEntity.getColor() != null) {
                continue;
            }
            ColumnColorEnum color = Arrays.stream(palette)
                    .filter(c -> !usedColors.contains(c))
                    .findFirst()
                    .orElse(palette[nextRepeatedColor++ % palette.length]);
            columnEntity.setColor(color);
            usedColors.add(color);
        }
    }

    public void updateProject(UUID projectId, UpdateProjectRequestDTO requestDTO, UUID userId) {
        ProjectEntity projectEntity = projectRepository.findById(projectId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("projectId", projectId)
                        .entityType("ProjectEntity")
                        .build());
        projectEntity.setDescription(requestDTO.description());

        Map<UUID, ColumnEntity> existingColumns = columnRepository.findByProjectIdOrderByPosition(projectId)
                .stream()
                .collect(Collectors.toMap(ColumnEntity::getId, Function.identity()));

        List<UpdateColumnRequestDTO> requestedColumns = requestDTO.columns();
        Set<UUID> keptColumnIds = requestedColumns.stream()
                .map(UpdateColumnRequestDTO::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (UUID columnId : keptColumnIds) {
            if (!existingColumns.containsKey(columnId)) {
                throw EntityNotFoundException.builder()
                        .identifier("columnId", columnId)
                        .identifier("projectId", projectId)
                        .entityType("ColumnEntity")
                        .build();
            }
        }

        deleteRemovedColumns(existingColumns, keptColumnIds);

        // Positions are unique within a project, so the kept columns first move to free negative positions.
        // Otherwise swapping two columns would break the constraint in the middle of the update.
        for (short i = 0; i < requestedColumns.size(); i++) {
            UUID columnId = requestedColumns.get(i).id();
            if (columnId != null) {
                existingColumns.get(columnId).setPosition((short) -(i + 1));
            }
        }
        columnRepository.flush();

        List<ColumnEntity> columnEntities = new ArrayList<>();
        for (short i = 0; i < requestedColumns.size(); i++) {
            UpdateColumnRequestDTO requestedColumn = requestedColumns.get(i);
            ColumnEntity columnEntity;
            if (requestedColumn.id() == null) {
                columnEntity = new ColumnEntity();
                columnEntity.setProject(projectEntity);
                columnEntity.setColor(requestedColumn.color());
            } else {
                columnEntity = existingColumns.get(requestedColumn.id());
            }
            columnEntity.setName(requestedColumn.name());
            columnEntity.setPosition((short)(i*100));
            columnEntities.add(columnEntity);
        }
        assignDefaultColors(columnEntities);
        columnRepository.saveAll(columnEntities);

        log.info("Project updated [projectId={}, userId={}]",
                projectId,
                userId);
    }

    private void deleteRemovedColumns(Map<UUID, ColumnEntity> existingColumns, Set<UUID> keptColumnIds) {
        List<ColumnEntity> removedColumns = existingColumns.values().stream()
                .filter(column -> !keptColumnIds.contains(column.getId()))
                .toList();
        for (ColumnEntity column : removedColumns) {
            if (taskRepository.existsByColumnIdAndStage(column.getId(), TaskStageEnum.BOARD)) {
                throw new ColumnNotEmptyException(column.getName());
            }
        }
        columnRepository.deleteAll(removedColumns);
        columnRepository.flush();
    }

    private void setDefaultProjectIdForUser(UserEntity userEntity, ProjectEntity savedProjectEntity) {
            userEntity.setProject(savedProjectEntity);
            userRepository.save(userEntity);
    }

    public Project findProjectById(UUID projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectEntityMapper.INSTANCE::toProject)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("projectId", projectId)
                        .entityType("ProjectEntity")
                        .build());
    }

    public UserProjects findProjectsByUserEmail(String email) {

        // Both queries run on applicationTaskExecutor.
        // Thanks to that, their spans appear in the trace of this request instead of in separate traces.
        CompletableFuture<List<UserProjectQueryResponseDTO>> userProjectsCF = CompletableFuture.supplyAsync(
                () -> customProjectRepository.findProjectsByUserEmail(email),
                applicationTaskExecutor
        );

        CompletableFuture<Optional<UUID>> defaultUserProjectCF = CompletableFuture.supplyAsync(
                () -> userRepository.findDefaultProjectIdByEmail(email),
                applicationTaskExecutor
        );

        CompletableFuture<Void> cfs = CompletableFuture.allOf(userProjectsCF, defaultUserProjectCF);
        cfs.join();

        List<UserProjectQueryResponseDTO> projects = userProjectsCF.join();
        Optional<UUID> defaultProjectId = defaultUserProjectCF.join();

        moveDefaultProjectToFront(defaultProjectId, projects);

        return new UserProjects(projects);
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

    public boolean isUserAdminInProject(UUID userId, UUID projectId) {
        return customProjectRepository.isUserAdminInProject(userId, projectId);
    }

    public boolean hasAtLeastMemberRole(String email, UUID projectId) {
        return customProjectRepository.hasAtLeastMemberRole(email, projectId);
    }

    public AssignUserToProject assignUserToProjectAndHandleUserDefaultProject(UUID projectId, String userEmail, ProjectRoleEnum userRoleToBeAssigned) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("projectId", projectId)
                        .entityType("ProjectEntity")
                        .build());

        UserEntity userToAssign = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("email", userEmail)
                        .entityType("UserEntity")
                        .build());

        // check if user already is in project
        if(customProjectRepository.ifUserIsInProject(userEmail, projectId)) {
            throw new UserAlreadyInProjectException(userEmail, project.getName());
        }

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
        projectUsersEntity.setProjectRole(userRoleToBeAssigned);
        projectUsersRepository.save(projectUsersEntity);

        log.info("User assigned to project [projectId={}, userId={}, role={}]",
                projectId,
                userToAssign.getId(),
                userRoleToBeAssigned);

        return new AssignUserToProject(
                projectId,
                userToAssign.getId(),
                userRoleToBeAssigned.getRole()
        );
    }

    public void removeUserFromProject(UUID projectId, UUID userId) {
        // Find user first by email
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("id", userId)
                        .entityType("UserEntity")
                        .build());

        // Check if user exists in project
        ProjectUsersEntity projectUser = projectUsersRepository
                .findById(new ProjectUserId(projectId, user.getId()))
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("ProjectUserId", new ProjectUserId(projectId, user.getId()))
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

        log.info("User removed from project [projectId={}, userId={}]",
                projectId,
                userId);

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

        log.info("User role changed [projectId={}, userId={}, oldRole={}, newRole={}]",
                projectId,
                userId,
                currentRole,
                newRole);

        return new ChangeUserRoleResponseDTO(
                projectId,
                userId,
                newRole.getRole()
        );
    }

}
