package com.stodo.projectchaos.ai;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test-llm")
public class AIController {
    private final AIChatService aiChatService;

    public AIController(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> talk(
        @RequestHeader(name="X_AI_CONVERSATION_ID", defaultValue = "default") UUID conversationId,
        @RequestBody Question question,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        return aiChatService.talk(question.content(), conversationId, userEmail);
    }
}
