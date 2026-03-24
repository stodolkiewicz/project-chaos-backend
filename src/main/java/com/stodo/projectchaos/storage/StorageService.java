package com.stodo.projectchaos.storage;

import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class StorageService {
    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

    public StorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public String uploadFile(MultipartFile file, UUID projectId, UUID taskId) throws IOException {
        String objectPath = String.format("projects/%s/tasks/%s/%s",
                            projectId, taskId, file.getOriginalFilename());

        BlobId blobId = BlobId.of(bucketName, objectPath );
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        Blob blob = storage.create(blobInfo, file.getBytes());


        return objectPath;
    }
}
