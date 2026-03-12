package com.stodo.projectchaos.ai;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/test-llm")
public class AIController {
    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> talk(
        @RequestHeader(name="X_AI_CONVERSATION_ID", defaultValue = "default") String conversationId,
        @RequestBody Question question
    ) {
        return aiService.talk(question.content(), conversationId);
    }
}
