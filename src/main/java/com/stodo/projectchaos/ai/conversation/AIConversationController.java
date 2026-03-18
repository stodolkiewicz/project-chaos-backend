package com.stodo.projectchaos.ai.conversation;

import com.stodo.projectchaos.ai.conversation.dto.mapper.AIConversationMapper;
import com.stodo.projectchaos.ai.conversation.dto.response.ConversationResponseDTO;
import com.stodo.projectchaos.ai.conversation.dto.service.AIConversation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AIConversationController {

    private final AIConversationService aiConversationService;

    @GetMapping("/projects/{projectId}/users/{userId}/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getConversations(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        List<AIConversation> conversations = aiConversationService.getConversationsByProjectIdAndUserId(projectId, userId);
        List<ConversationResponseDTO> response = AIConversationMapper.INSTANCE.toConversationResponseDTOList(conversations);
        return ResponseEntity.ok(response);
    }
}