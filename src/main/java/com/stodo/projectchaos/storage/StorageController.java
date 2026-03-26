package com.stodo.projectchaos.storage;

import com.stodo.projectchaos.features.attachment.AttachmentInfo;
import com.stodo.projectchaos.features.attachment.dto.mapper.AttachmentMapper;
import com.stodo.projectchaos.storage.dto.TaskAttachmentsResponseDTO;
import com.stodo.projectchaos.features.user.UserService;
import com.stodo.projectchaos.security.annotation.CurrentUserId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class StorageController {

    private final AttachmentManager attachmentManager;
    private final UserService userService;

    public StorageController(AttachmentManager attachmentManager, UserService userService) {
        this.attachmentManager = attachmentManager;
        this.userService = userService;
    }

    @PreAuthorize("@projectSecurity.hasAtLeastMemberRole(#projectId, authentication)")
    @PostMapping("/projects/{projectId}/tasks/{taskId}/storage/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @CurrentUserId UUID userId
    ) throws Exception {

        String objectPath = attachmentManager.uploadFile(file, projectId, userId, taskId);
        return ResponseEntity.created(URI.create(objectPath)).build();
    }
    //https://www.linkedin.com/pulse/integrating-google-cloud-storage-spring-boot-junior-nakamura-cpumf/
    // https://github.com/GoogleCloudPlatform/spring-cloud-gcp/blob/main/spring-cloud-gcp-samples/spring-cloud-gcp-storage-resource-sample/src/main/java/com/example/WebController.java

    @GetMapping("/projects/{projectId}/tasks/{taskId}/storage/urls")
    public ResponseEntity<TaskAttachmentsResponseDTO> getPresignedUrls(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId
    ) {
        List<AttachmentInfo> attachments = attachmentManager.generatePresignedUrl(projectId, taskId);
        
        TaskAttachmentsResponseDTO response = new TaskAttachmentsResponseDTO(
                attachments.stream()
                        .map(AttachmentMapper.INSTANCE::toAttachmentResponseDTO)
                        .toList()
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@projectSecurity.hasAtLeastMemberRole(#projectId, authentication)")
    @DeleteMapping("/projects/{projectId}/tasks/{taskId}/storage/{attachmentId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID attachmentId
    ) {
        boolean deleted = attachmentManager.deleteFile(projectId, taskId, attachmentId);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }
}
