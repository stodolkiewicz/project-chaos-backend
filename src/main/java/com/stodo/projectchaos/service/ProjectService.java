package com.stodo.projectchaos.service;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.project.byid.response.ProjectMapper;
import com.stodo.projectchaos.model.dto.project.create.request.CreateProjectRequestDTO;
import com.stodo.projectchaos.model.dto.project.create.response.CreateProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.byid.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.query.UserProjectQueryResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.response.UserProjectsResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.query.SimpleProjectQueryResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.response.SimpleProjectsResponseDTO;
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
public class ProjectService {

    private final CustomProjectRepository customProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUsersRepository projectUsersRepository;
    private final ColumnRepository columnRepository;
    private final TaskPriorityRepository taskPriorityRepository;

    public ProjectService(CustomProjectRepository customProjectRepository, ProjectRepository projectRepository, UserRepository userRepository, ProjectUsersRepository projectUsersRepository, ColumnRepository columnRepository, TaskPriorityRepository taskPriorityRepository) {
        this.customProjectRepository = customProjectRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectUsersRepository = projectUsersRepository;
        this.columnRepository = columnRepository;
        this.taskPriorityRepository = taskPriorityRepository;
    }

    @Transactional
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
        projectUsersEntity.setId(new ProjectUserId(savedProjectEntity.getId(), email));
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

        return new UserProjectsResponseDTO(projects, defaultProjectId.orElse(null));
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

    public SimpleProjectsResponseDTO findSimpleProjectsByUserEmail(String email) {
        List<SimpleProjectQueryResponseDTO> projects = customProjectRepository.findSimpleProjectsByUserEmail(email);
        return new SimpleProjectsResponseDTO(projects);
    }

    public boolean isUserAdminInProject(String email, UUID projectId) {
       return customProjectRepository.isUserAdminInProject(email, projectId);
    }
}
