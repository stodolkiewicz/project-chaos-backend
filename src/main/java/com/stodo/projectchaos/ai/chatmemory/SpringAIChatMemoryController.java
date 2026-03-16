package com.stodo.projectchaos.ai.chatmemory;

import com.stodo.projectchaos.ai.chatmemory.dto.mapper.ChatMemoryMapper;
import com.stodo.projectchaos.ai.chatmemory.dto.response.ChatMemoryResponseDTO;
import com.stodo.projectchaos.ai.chatmemory.dto.service.ChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat-memory")
@RequiredArgsConstructor
public class SpringAIChatMemoryController {

    private final SpringAIChatMemoryService service;

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<ChatMemoryResponseDTO>> getChatHistory(
            @PathVariable String conversationId) {
        List<ChatMemory> chatHistory = service.getChatHistory(conversationId);
        List<ChatMemoryResponseDTO> response = ChatMemoryMapper.INSTANCE.toChatMemoryResponseDTOList(chatHistory);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<Void> deleteChatHistory(@PathVariable String conversationId) {
        service.deleteChatHistory(conversationId);
        return ResponseEntity.noContent().build();
    }
}