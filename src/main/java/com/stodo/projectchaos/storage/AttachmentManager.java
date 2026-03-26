package com.stodo.projectchaos.storage;

import com.stodo.projectchaos.exception.FileTooLargeException;
import com.stodo.projectchaos.exception.StorageLimitExceededException;
import com.stodo.projectchaos.features.project.ProjectRepository;
import com.stodo.projectchaos.features.task.AttachmentRepository;
import com.stodo.projectchaos.features.task.TaskRepository;
import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.model.entity.*;
import com.stodo.projectchaos.model.enums.VectorStatusEnum;
import com.stodo.projectchaos.storage.projectlimit.ProjectLimitService;
import com.stodo.projectchaos.storage.projectlimit.dto.service.ProjectStorageUsage;
import com.stodo.projectchaos.storage.userlimit.UserLimitService;
import com.stodo.projectchaos.storage.userlimit.dto.service.UserStorageUsage;
import com.stodo.projectchaos.storage.utils.FileNameSanitizer;
import com.stodo.projectchaos.storage.utils.FileTextExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AttachmentManager {

    private final StorageService storageService;
    private final ProjectLimitService projectLimitService;
    private final UserLimitService userLimitService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AttachmentRepository attachmentRepository;

    @Value("${storage.limits.file.default}")
    private Long singleFileUploadLimitInBytes;

    public AttachmentManager(StorageService storageService, ProjectLimitService projectLimitService, UserLimitService userLimitService, ProjectRepository projectRepository, UserRepository userRepository, TaskRepository taskRepository, AttachmentRepository attachmentRepository) {
        this.storageService = storageService;
        this.projectLimitService = projectLimitService;
        this.userLimitService = userLimitService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.attachmentRepository = attachmentRepository;
    }

    @Transactional
    public String uploadFile(MultipartFile file, UUID projectId, UUID userId, UUID taskId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String sanitizedFilename = FileNameSanitizer.sanitize(originalFilename);
        String cloudStorageFilename = UUID.randomUUID() + "_" + sanitizedFilename;

        String contentType = file.getContentType();
        Long bytesToBeUploaded = file.getSize();
        checkStorageLimits(originalFilename, bytesToBeUploaded, projectId, userId);

        String filePath = String.format("projects/%s/tasks/%s/%s", projectId, taskId, cloudStorageFilename);

        storageService.uploadFile(file, filePath);

        projectLimitService.increaseUsedBytes(projectId, bytesToBeUploaded);
        userLimitService.increaseUsedBytes(userId, bytesToBeUploaded);

        ProjectEntity projectEntityReference = projectRepository.getReferenceById(projectId);
        TaskEntity taskEntityReference = taskRepository.getReferenceById(taskId);
        UserEntity userEntityReference = userRepository.getReferenceById(userId);
        String extractedText = "";
        try {
            extractedText = FileTextExtractor.extractText(file.getBytes());
        } catch (TikaException | SAXException e) {
            log.warn("Text could not be extracted from {}.", originalFilename);
        }

        // todo: save attachment entity
        AttachmentEntity attachment = AttachmentEntity.builder()
                .id(UUID.randomUUID()) // Identyfikator w Twojej bazie
                .project(projectEntityReference)
                .task(taskEntityReference)
                .user(userEntityReference)
                .fileName(cloudStorageFilename)
                .originalName(originalFilename)
                .filePath(filePath)
                .contentType(contentType)
                .fileSizeInBytes(bytesToBeUploaded)
                .vectorStatus(VectorStatusEnum.PENDING)
                .extractedText(extractedText)
                .build();

        attachmentRepository.save(attachment);
        return filePath;
    }

    private void checkStorageLimits(String filename, Long bytesToBeUploaded , UUID projectId, UUID userId) {
        CompletableFuture<ProjectStorageUsage> projectStorageCF =
                CompletableFuture.supplyAsync(() -> projectLimitService.getProjectStorageUsage(projectId));
        CompletableFuture<UserStorageUsage> userStorageCF =
                CompletableFuture.supplyAsync(() -> userLimitService.getUserStorageUsage(userId));
        CompletableFuture<Void> storagesCF = CompletableFuture.allOf(projectStorageCF, userStorageCF);
        storagesCF.join();
        ProjectStorageUsage projectStorageUsage = projectStorageCF.join();
        UserStorageUsage userStorageUsage = userStorageCF.join();

        boolean projectStorageLimitOk = projectStorageUsage.usedBytes() + bytesToBeUploaded <= projectStorageUsage.limitBytes();
        boolean userStorageLimitOk = userStorageUsage.usedBytes() + bytesToBeUploaded <= userStorageUsage.limitBytes();
        boolean singleFileLimitOk = singleFileUploadLimitInBytes > bytesToBeUploaded;

        if(!projectStorageLimitOk) {
            throw StorageLimitExceededException.builder()
                    .entityType("Project")
                    .bytesToBeUploaded(bytesToBeUploaded)
                    .limitInBytes(projectStorageUsage.limitBytes())
                    .usedBytes(projectStorageUsage.usedBytes())
                    .build();
        }

        if(!userStorageLimitOk) {
            throw StorageLimitExceededException.builder()
                    .entityType("User")
                    .bytesToBeUploaded(bytesToBeUploaded)
                    .limitInBytes(userStorageUsage.limitBytes())
                    .usedBytes(userStorageUsage.usedBytes())
                    .build();
        }

        if(!singleFileLimitOk) {
            throw new FileTooLargeException(filename, singleFileUploadLimitInBytes);
        }
    }

}
