package com.stodo.projectchaos.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SPRING_AI_CHAT_MEMORY")
public class SpringAIChatMemoryEntity {
    @Builder.Default
    @Id
    private UUID id = UUID.randomUUID();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AIConversationEntity conversation;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private MessageType type;
    
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    
    public enum MessageType {
        USER, ASSISTANT, SYSTEM, TOOL
    }
}
