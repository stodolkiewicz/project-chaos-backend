package com.stodo.projectchaos.ai.chat.config;

import com.stodo.projectchaos.ai.tools.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    private final JdbcChatMemoryRepository chatMemoryRepository;
    private final ProjectInfoTools projectInfoTools;
    private final CurrentUserInfoTools currentUserInfoTools;
    private final TavilyWebSearchTool tavilyWebSearchTool;
    private final VectorSearchTools vectorSearchTools;

    public AIConfig(JdbcChatMemoryRepository chatMemoryRepository, ProjectInfoTools projectInfoTools, CurrentUserInfoTools currentUserInfoTools, TavilyWebSearchTool tavilyWebSearchTool, VectorSearchTools vectorSearchTools) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.projectInfoTools = projectInfoTools;
        this.currentUserInfoTools = currentUserInfoTools;
        this.tavilyWebSearchTool = tavilyWebSearchTool;
        this.vectorSearchTools = vectorSearchTools;
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
            .defaultTools(new DateTimeTools(), projectInfoTools, currentUserInfoTools, tavilyWebSearchTool, vectorSearchTools)
            .build();
    }

    @Bean
    ChatMemory messageWindowChatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    // Provide llm with feedback on failed tool calls
    @Bean
    public ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
        return (exception) -> {
            // Check if the error is a UUID parsing issue
            if (exception.getCause() instanceof IllegalArgumentException &&
                    exception.getMessage().contains("UUID")) {

                return "ERROR: The provided UUID is invalid. " +
                        "Please ensure it is exactly 36 characters long and follows the 8-4-4-4-12 pattern. " +
                        "Example: 123e4567-e89b-12d3-a456-426614174000";
            }

            return "Tool execution error: " + exception.getMessage();
        };
    }

}
