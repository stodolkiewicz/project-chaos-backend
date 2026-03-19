package com.stodo.projectchaos.ai.features.chatmemory;

import com.stodo.projectchaos.ai.features.chatmemory.dto.mapper.ChatMemoryMapper;
import com.stodo.projectchaos.ai.features.chatmemory.dto.response.ChatMemoryResponseDTO;
import com.stodo.projectchaos.ai.features.chatmemory.dto.service.ChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SpringAIChatMemoryController {

    private final SpringAIChatMemoryService service;

    @GetMapping("/projects/{projectId}/users/{userId}/conversation/{conversationId}")
    public ResponseEntity<List<ChatMemoryResponseDTO>> getChatHistory(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @PathVariable String conversationId) {
        List<ChatMemory> chatHistory = service.getChatHistory(conversationId, projectId, userId);
        List<ChatMemoryResponseDTO> response = ChatMemoryMapper.INSTANCE.toChatMemoryResponseDTOList(chatHistory);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/projects/{projectId}/users/{userId}/conversation/{conversationId}")
    public ResponseEntity<Void> deleteChatHistory(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @PathVariable String conversationId) {
        service.deleteChatHistory(conversationId, projectId, userId);
        return ResponseEntity.noContent().build();
    }
}