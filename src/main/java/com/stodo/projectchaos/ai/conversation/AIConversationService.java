package com.stodo.projectchaos.ai.conversation;

import com.stodo.projectchaos.ai.conversation.dto.mapper.AIConversationEntityMapper;
import com.stodo.projectchaos.ai.conversation.dto.service.AIConversation;
import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.project.ProjectRepository;
import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.model.entity.AIConversationEntity;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIConversationService {

    private final AIConversationRepository conversationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public AIConversation createConversation(UUID projectId, UUID userId, String conversationId, String title) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("id", projectId)
                        .entityType("ProjectEntity")
                        .build());

        UserEntity user = userRepository.getReferenceById(userId);

        AIConversationEntity entity = AIConversationEntity.builder()
                .id(conversationId)
                .project(project)
                .user(user)
                .title(title)
                .build();

        AIConversationEntity savedEntity = conversationRepository.save(entity);
        conversationRepository.flush();
        return AIConversationEntityMapper.INSTANCE.toAIConversation(savedEntity);
    }

    @Transactional
    public void updateConversationTitle(String conversationId, String newTitle) {
        AIConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("id", conversationId)
                        .entityType("AIConversationEntity")
                        .build());

        conversation.setTitle(newTitle);
        conversation.setConversationHasTitle(true);
    }

    // todo - preauthorize method to check if it is called by the user with userId in the parameters
    public List<AIConversation> getConversationsByProjectIdAndUserId(UUID projectId, UUID userId) {
        if (!projectRepository.existsById(projectId)) {
            throw EntityNotFoundException.builder()
                    .identifier("id", projectId)
                    .entityType("ProjectEntity")
                    .build();
        }

        List<AIConversationEntity> entities = conversationRepository.findByProjectIdAndUserIdOrderByCreatedAtDesc(projectId, userId);
        return AIConversationEntityMapper.INSTANCE.toAIConversationList(entities);
    }

    @Transactional
    public void deleteConversation(String conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw EntityNotFoundException.builder()
                    .identifier("id", conversationId)
                    .entityType("AIConversationEntity")
                    .build();
        }
        conversationRepository.deleteById(conversationId);
    }

    public boolean checkIfExistsByConversationId(String conversationId) {
        return conversationRepository.existsById(conversationId);
    }

    public Optional<AIConversation> findByConversationId(String conversationId) {
        return conversationRepository.findById(conversationId)
                .map(AIConversationEntityMapper.INSTANCE::toAIConversation);
    }
}