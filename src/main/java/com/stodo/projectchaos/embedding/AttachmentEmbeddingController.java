package com.stodo.projectchaos.embedding;

import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AttachmentEmbeddingController {

    private final AttachmentEmbeddingService attachmentEmbeddingService;

    public AttachmentEmbeddingController(AttachmentEmbeddingService attachmentEmbeddingService) {
        this.attachmentEmbeddingService = attachmentEmbeddingService;
    }

    @PostMapping("/projects/{projectId}/tasks/{taskId}/similar")
    public ResponseEntity<List<Document>> searchSimilarInTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int numberOfResults) {
        
        List<Document> results = attachmentEmbeddingService.search(query, numberOfResults, projectId, taskId);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/projects/{projectId}/similar")
    public ResponseEntity<List<Document>> searchSimilarInProject(
            @PathVariable UUID projectId,
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int numberOfResults) {
        
        List<Document> results = attachmentEmbeddingService.search(query, numberOfResults, projectId, null);
        return ResponseEntity.ok(results);
    }
}
