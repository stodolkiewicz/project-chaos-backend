package com.stodo.projectchaos.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    final
    JdbcChatMemoryRepository chatMemoryRepository;

    public AIConfig(JdbcChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }

    @Bean
    ChatClient chatClient(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory
    ) {

        var options = OpenAiChatOptions.builder()
            .temperature(0.8)
            .streamUsage(true)
            .build();

        return chatClientBuilder
            .defaultOptions(options)
            .defaultAdvisors(
                    MessageChatMemoryAdvisor.builder(chatMemory).build()
            ).build();
    }

    ChatMemory messageWindowChatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }

}
