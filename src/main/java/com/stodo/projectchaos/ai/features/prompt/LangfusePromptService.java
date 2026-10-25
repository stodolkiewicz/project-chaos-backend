package com.stodo.projectchaos.ai.features.prompt;

import com.stodo.projectchaos.ai.features.prompt.dto.response.LangfusePromptResponseDTO;
import com.stodo.projectchaos.ai.features.prompt.dto.service.SystemPrompt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

/**
 * Provides the system prompt of the AI chat.
 * The prompt is fetched from the Langfuse public API and kept in memory for the configured time to live.
 * The file promptTemplates/system-prompt.st is the fallback when Langfuse is not configured or not reachable.
 */
@Slf4j
@Service
public class LangfusePromptService {
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(2);

    private final RestClient restClient;
    private final boolean enabled;
    private final String promptName;
    private final String promptLabel;
    private final Duration cacheTtl;
    private final Resource fallbackTemplate;

    private volatile CachedPrompt cachedPrompt;

    public LangfusePromptService(
            @Value("${app.langfuse.base-url:https://cloud.langfuse.com}") String baseUrl,
            @Value("${app.langfuse.auth:}") String auth,
            @Value("${app.langfuse.system-prompt.name:system-prompt}") String promptName,
            @Value("${app.langfuse.system-prompt.label:production}") String promptLabel,
            @Value("${app.langfuse.system-prompt.cache-ttl-seconds:60}") long cacheTtlSeconds,
            @Value("classpath:/promptTemplates/system-prompt.st") Resource fallbackTemplate
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(HTTP_TIMEOUT);
        requestFactory.setReadTimeout(HTTP_TIMEOUT);

        // LANGFUSE_AUTH is the base64 value of "publicKey:secretKey", which is the value of the Basic Auth header.
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Basic " + auth)
                .build();
        this.enabled = !auth.isBlank();
        this.promptName = promptName;
        this.promptLabel = promptLabel;
        this.cacheTtl = Duration.ofSeconds(cacheTtlSeconds);
        this.fallbackTemplate = fallbackTemplate;
    }

    public SystemPrompt getSystemPrompt() {
        if (!enabled) {
            return readFallback();
        }

        CachedPrompt current = cachedPrompt;
        if (current != null && current.isFresh(cacheTtl)) {
            return current.prompt();
        }

        return refresh(current);
    }

    // Only one thread fetches the prompt. The other threads wait and then use the fresh value.
    private synchronized SystemPrompt refresh(CachedPrompt previous) {
        CachedPrompt current = cachedPrompt;
        if (current != null && current.isFresh(cacheTtl)) {
            return current.prompt();
        }

        try {
            SystemPrompt fetched = fetch();
            cachedPrompt = new CachedPrompt(fetched, Instant.now());
            log.info("System prompt fetched from Langfuse [name={}, version={}]", fetched.name(), fetched.version());
            return fetched;
        } catch (RuntimeException e) {
            // A stale prompt is better than no prompt. The next call tries to fetch again.
            if (previous != null) {
                log.warn("Fetching the system prompt from Langfuse failed, using the cached version [name={}, version={}]",
                        previous.prompt().name(),
                        previous.prompt().version(),
                        e);
                return previous.prompt();
            }
            log.warn("Fetching the system prompt from Langfuse failed, using the fallback file", e);
            return readFallback();
        }
    }

    private SystemPrompt fetch() {
        LangfusePromptResponseDTO response = restClient.get()
                .uri("/api/public/v2/prompts/{name}?label={label}",
                        UriUtils.encodePathSegment(promptName, StandardCharsets.UTF_8),
                        promptLabel)
                .retrieve()
                .body(LangfusePromptResponseDTO.class);

        if (response == null || !"text".equals(response.type()) || response.prompt() == null) {
            throw new IllegalStateException("The Langfuse prompt is missing or is not of the type text: " + promptName);
        }

        return new SystemPrompt(toSpringAiTemplate(response.prompt()), response.name(), response.version());
    }

    // Langfuse variables look like {{name}}. The template of Spring AI uses {name}.
    private String toSpringAiTemplate(String langfuseText) {
        return langfuseText.replaceAll("\\{\\{\\s*(\\w+)\\s*}}", "{$1}");
    }

    private SystemPrompt readFallback() {
        try {
            return new SystemPrompt(fallbackTemplate.getContentAsString(StandardCharsets.UTF_8), null, null);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read the fallback system prompt", e);
        }
    }

    private record CachedPrompt(SystemPrompt prompt, Instant fetchedAt) {
        boolean isFresh(Duration ttl) {
            return fetchedAt.plus(ttl).isAfter(Instant.now());
        }
    }
}
