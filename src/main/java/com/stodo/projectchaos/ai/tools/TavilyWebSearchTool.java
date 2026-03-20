package com.stodo.projectchaos.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class TavilyWebSearchTool {
    private final RestClient restClient;

    @Value("${tavily.api.key}")
    String apiKey;

    public TavilyWebSearchTool() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.tavily.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Tool(description = "Search the internet")
    public String searchWeb(
            @ToolParam(description = "Query to search the internet", required = true) String query
    ) {
        Map<String, Object> body = Map.of(
                "api_key", apiKey,
                "query", query,
                "search_depth", "basic",
                "max_results", 3
        );

        return restClient.post()
                .uri("/search")
                .body(body)
                .retrieve()
                .body(String.class);
    }
}
