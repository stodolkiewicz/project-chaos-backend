package com.stodo.projectchaos.ai.chat.config;

import com.stodo.projectchaos.ai.tools.CurrentUserInfoTools;
import com.stodo.projectchaos.ai.tools.DateTimeTools;
import com.stodo.projectchaos.ai.tools.ProjectInfoTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
public class AIConfig {

    private final JdbcChatMemoryRepository chatMemoryRepository;
    private final ProjectInfoTools projectInfoTools;
    private final CurrentUserInfoTools currentUserInfoTools;

    public AIConfig(JdbcChatMemoryRepository chatMemoryRepository, ProjectInfoTools projectInfoTools, CurrentUserInfoTools currentUserInfoTools) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.projectInfoTools = projectInfoTools;
        this.currentUserInfoTools = currentUserInfoTools;
    }

    @Bean
    ChatClient simpleChatClient(ChatClient.Builder builder) {
        return builder.build();
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
            .defaultTools(new DateTimeTools(), projectInfoTools, currentUserInfoTools)
            .build();
    }

    @Bean
    ChatMemory messageWindowChatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }

}
