package com.stodo.projectchaos.embedding;

import com.stodo.projectchaos.embedding.dto.mapper.AttachmentEmbeddingEntityMapper;
import com.stodo.projectchaos.embedding.dto.service.AttachmentEmbedding;
import com.stodo.projectchaos.features.attachment.AttachmentService;
import com.stodo.projectchaos.features.project.ProjectRepository;
import com.stodo.projectchaos.features.task.TaskRepository;
import com.stodo.projectchaos.model.entity.AttachmentEmbeddingEntity;
import com.stodo.projectchaos.model.entity.AttachmentEntity;
import com.stodo.projectchaos.model.enums.VectorStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class AttachmentEmbeddingService {

    private final AttachmentEmbeddingRepository attachmentEmbeddingRepository;
    private final AttachmentService attachmentService;
    private final EmbeddingModel embeddingModel;
    private final AppTokenTextSplitter tokenTextSplitter;
    private final VectorStore vectorStore;


    public AttachmentEmbeddingService(AttachmentEmbeddingRepository attachmentEmbeddingRepository, AttachmentService attachmentService, EmbeddingModel embeddingModel, AppTokenTextSplitter tokenTextSplitter, TaskRepository taskRepository, ProjectRepository projectRepository, VectorStore vectorStore) {
        this.attachmentEmbeddingRepository = attachmentEmbeddingRepository;
        this.attachmentService = attachmentService;
        this.embeddingModel = embeddingModel;
        this.tokenTextSplitter = tokenTextSplitter;
        this.vectorStore = vectorStore;
    }

    public List<Document> search(String query, int numberOfResults, UUID projectId, UUID taskId) {
        var parser = new FilterExpressionTextParser();

        String filterString = String.format("projectId == '%s'", projectId);

        if (taskId != null) {
            filterString += String.format(" && taskId == '%s'", taskId);
        }

        return vectorStore.similaritySearch(SearchRequest.builder()
                .filterExpression(parser.parse(filterString))
                .query(query)
                .similarityThreshold(0.1)
                .topK(numberOfResults)
                .build());
    }

    @Transactional
    public void embedAttachment(UUID attachmentId) {
        try {
            AttachmentEntity attachmentEntity = attachmentService.findById(attachmentId);
            Document document = new Document(attachmentEntity.getExtractedText());

            List<String> chunks = tokenTextSplitter.split(List.of(document))
                    .stream()
                    .map(Document::getText)
                    // clean up spaces, new lines
                    .map(text -> text
                            .replaceAll("\\s{2,}", " ")
                            .trim()
                    )
                    .collect(toList());

            List<float[]> vectors = embeddingModel.embed(chunks);

            List<AttachmentEmbeddingEntity> embeddings = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                AttachmentEmbeddingEntity attachmentEmbeddingEntity = new AttachmentEmbeddingEntity();
                attachmentEmbeddingEntity.setAttachment(attachmentEntity);
                HashMap<String, Object> metadata = new HashMap<>();
                if (attachmentEntity.getTask() != null) {
                    attachmentEmbeddingEntity.setTask(attachmentEntity.getTask());
                    metadata.put("taskId", attachmentEntity.getTask().getId());
                }
                metadata.put("projectId", attachmentEntity.getProject().getId());

                metadata.put("fileSizeInBytes", attachmentEntity.getFileSizeInBytes());
                metadata.put("filename", attachmentEntity.getOriginalName());

                attachmentEmbeddingEntity.setMetadata(metadata);

                attachmentEmbeddingEntity.setProject(attachmentEntity.getProject());
                attachmentEmbeddingEntity.setChunkIndex(i);
                attachmentEmbeddingEntity.setContent(chunks.get(i));
                attachmentEmbeddingEntity.setEmbedding(vectors.get(i));
                embeddings.add(attachmentEmbeddingEntity);
            }

            attachmentEmbeddingRepository.saveAll(embeddings);
            attachmentEntity.setVectorStatus(VectorStatusEnum.PROCESSED);

        } catch (Exception e) {
            log.error("Failed to embed attachment {}", attachmentId, e);
            markAsFailed(attachmentId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(UUID attachmentId) {
        AttachmentEntity attachment = attachmentService.findById(attachmentId);
        attachment.setVectorStatus(VectorStatusEnum.FAILED);
    }

    public List<AttachmentEmbedding> findByProjectId(UUID projectId) {
        List<AttachmentEmbeddingEntity> entities = attachmentEmbeddingRepository.findByProjectId(projectId);
        return entities.stream()
                .map(AttachmentEmbeddingEntityMapper.INSTANCE::toAttachmentEmbedding)
                .toList();
    }

    public List<AttachmentEmbedding> findByTaskId(UUID taskId) {
        List<AttachmentEmbeddingEntity> entities = attachmentEmbeddingRepository.findByTaskId(taskId);
        return entities.stream()
                .map(AttachmentEmbeddingEntityMapper.INSTANCE::toAttachmentEmbedding)
                .toList();
    }
}