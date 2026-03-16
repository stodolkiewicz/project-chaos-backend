package com.stodo.projectchaos.ai.chat;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AIController {
    private final AIChatService aiChatService;

    public AIController(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping(
            value = "/projects/{projectId}/users/{userId}/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    Flux<String> talk(
        @RequestHeader(name="X_AI_CONVERSATION_ID", defaultValue = "default") String conversationId,
        @RequestBody ChatRequestDTO chatRequestDTO,
        @PathVariable UUID projectId,
        @PathVariable UUID userId
    ) {
            return aiChatService.createConversationAndChat(projectId, userId, conversationId, chatRequestDTO.content());
    }
}
