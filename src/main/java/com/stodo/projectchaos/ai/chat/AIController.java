package com.stodo.projectchaos.ai.chat;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

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
    Flux<ServerSentEvent<Object>> talk(
        @RequestHeader(name="X_AI_CONVERSATION_ID", defaultValue = "default") String conversationId,
        @RequestBody ChatRequestDTO chatRequestDTO,
        @PathVariable UUID projectId,
        @PathVariable UUID userId
    ) {
            // Tools emit task proposals into this sink while the answer is streamed
            Sinks.Many<ServerSentEvent<Object>> events = Sinks.many().unicast().onBackpressureBuffer();

            Flux<ServerSentEvent<Object>> tokenEvents = aiChatService.createConversationAndChat(
                            projectId,
                            userId,
                            conversationId,
                            chatRequestDTO.content(),
                            events
                    )
                    .map(token -> ServerSentEvent.<Object>builder()
                            .event("token")
                            .data(new TokenEventDTO(token))
                            .build())
                    // Without completing the sink, Flux.merge never completes and the SSE connection stays open
                    .doOnComplete(events::tryEmitComplete);

            return Flux.merge(
                    tokenEvents,
                    events.asFlux()
            );
    }
}
