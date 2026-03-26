package com.stodo.projectchaos.storage;

import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class StorageService {
    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

    public StorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public String uploadFile(MultipartFile file, String filePath) throws IOException {
        BlobId blobId = BlobId.of(bucketName, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();


        storage.create(blobInfo, file.getBytes());

        return filePath;
    }

    @Async
    public CompletableFuture<String> generatePresignedUrl(String filePath) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filePath).build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                15,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature()
        );

        return CompletableFuture.completedFuture(signedUrl.toString());
    }

    public boolean deleteFile(String filePath) {
        BlobId blobId = BlobId.of(bucketName, filePath);
        boolean isDeleted = storage.delete(blobId);

        return isDeleted;
    }

}
