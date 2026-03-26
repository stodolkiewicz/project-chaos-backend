package com.stodo.projectchaos.storage;

import com.stodo.projectchaos.features.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
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

    @PostMapping("/projects/{projectId}/tasks/{taskId}/storage/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            Authentication authentication
    ) throws Exception {
        String userEmail = authentication.getName();
        UUID userId = userService.getUserIdByEmail(userEmail);
        String objectPath = attachmentManager.uploadFile(file, projectId, userId, taskId);
        return ResponseEntity.created(URI.create(objectPath)).build();
    }
    //https://www.linkedin.com/pulse/integrating-google-cloud-storage-spring-boot-junior-nakamura-cpumf/
    // https://github.com/GoogleCloudPlatform/spring-cloud-gcp/blob/main/spring-cloud-gcp-samples/spring-cloud-gcp-storage-resource-sample/src/main/java/com/example/WebController.java

//    @GetMapping("/projects/{projectId}/tasks/{taskId}/storage/url")
//    public ResponseEntity<List<String>> getLinks() {
//        storageService.generatePresignedUrl()
//    }
}
