package com.stodo.projectchaos.ai.tools;

import com.stodo.projectchaos.embedding.AttachmentEmbeddingService;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class VectorSearchTools {

    private final AttachmentEmbeddingService attachmentEmbeddingService;

    public VectorSearchTools(AttachmentEmbeddingService attachmentEmbeddingService) {
        this.attachmentEmbeddingService = attachmentEmbeddingService;
    }

    @Tool(description = "Search across text of all files (PDF, DOCX, EPUB) attached to a project or a specific task. " +
            "Hierarchy: A project contains multiple tasks, and both can have attachments. " +
            "Use ONLY projectId to search everything in the project. " +
            "Add taskId to narrow the search to a specific task's files.")
    public List<Document> searchAttachments(
            @ToolParam(description = "The unique UUID of the project.", required = true) UUID projectId,
            @ToolParam(description = "The search query or question.", required = true) String query,
            @ToolParam(description = "Optional UUID of a specific task to narrow down the search.", required = false) UUID taskId,
            @ToolParam(description = "Maximum number of results to return (default 3).", required = false) Integer maxResults
    ) {
        int limit = (maxResults != null && maxResults > 0) ? maxResults : 3;
        return attachmentEmbeddingService.search(query, limit, projectId, taskId);
    }
}
