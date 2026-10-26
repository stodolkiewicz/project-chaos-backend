package com.stodo.projectchaos.ai.features.prompt;

import com.stodo.projectchaos.ai.features.prompt.dto.response.LangfusePromptResponseDTO;
import com.stodo.projectchaos.ai.features.prompt.dto.service.LangfusePrompt;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides text prompts stored in Langfuse.
 * A prompt is fetched from the Langfuse public API and kept in memory for the configured time to live.
 * The cache holds one entry per prompt name, so the service can be used for any number of prompts.
 * The caller passes a fallback file, which is used when Langfuse is not configured or not reachable.
 */
@Slf4j
@Service
public class LangfusePromptService {
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(2);

    private final RestClient restClient;
    private final boolean enabled;
    private final String promptLabel;
    private final Duration cacheTtl;

    private final ConcurrentHashMap<String, CachedPrompt> cache = new ConcurrentHashMap<>();
    // One lock per prompt name. A fetch of one prompt does not block the fetch of another prompt.
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public LangfusePromptService(
            @Value("${app.langfuse.base-url:https://cloud.langfuse.com}") String baseUrl,
            @Value("${app.langfuse.auth:}") String auth,
            @Value("${app.langfuse.prompt-label:production}") String promptLabel,
            @Value("${app.langfuse.cache-ttl-seconds:60}") long cacheTtlSeconds
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
        this.promptLabel = promptLabel;
        this.cacheTtl = Duration.ofSeconds(cacheTtlSeconds);
    }

    public LangfusePrompt getTextPrompt(String promptName, Resource fallbackTemplate) {
        if (!enabled) {
            return readFallback(fallbackTemplate);
        }

        CachedPrompt current = cache.get(promptName);
        if (current != null && current.isFresh(cacheTtl)) {
            return current.prompt();
        }

        // Only one thread fetches a given prompt. The other threads wait and then use the fresh value.
        synchronized (locks.computeIfAbsent(promptName, key -> new Object())) {
            current = cache.get(promptName);
            if (current != null && current.isFresh(cacheTtl)) {
                return current.prompt();
            }
            return refresh(promptName, current, fallbackTemplate);
        }
    }

    private LangfusePrompt refresh(String promptName, CachedPrompt previous, Resource fallbackTemplate) {
        try {
            LangfusePrompt fetched = fetch(promptName);
            cache.put(promptName, new CachedPrompt(fetched, Instant.now()));
            log.info("Prompt fetched from Langfuse [name={}, version={}]", fetched.name(), fetched.version());
            return fetched;
        } catch (RuntimeException e) {
            // A stale prompt is better than no prompt. The next call tries to fetch again.
            if (previous != null) {
                log.warn("Fetching the prompt from Langfuse failed, using the cached version [name={}, version={}]",
                        previous.prompt().name(),
                        previous.prompt().version(),
                        e);
                return previous.prompt();
            }
            log.warn("Fetching the prompt from Langfuse failed, using the fallback file [name={}]", promptName, e);
            return readFallback(fallbackTemplate);
        }
    }

    private LangfusePrompt fetch(String promptName) {
        LangfusePromptResponseDTO response = restClient.get()
                .uri("/api/public/v2/prompts/{name}?label={label}",
                        UriUtils.encodePathSegment(promptName, StandardCharsets.UTF_8),
                        promptLabel)
                .retrieve()
                .body(LangfusePromptResponseDTO.class);

        if (response == null || !"text".equals(response.type()) || response.prompt() == null) {
            throw new IllegalStateException("The Langfuse prompt is missing or is not of the type text: " + promptName);
        }

        return new LangfusePrompt(toSpringAiTemplate(response.prompt()), response.name(), response.version());
    }

    // Langfuse variables look like {{name}}. The template of Spring AI uses {name}.
    private String toSpringAiTemplate(String langfuseText) {
        return langfuseText.replaceAll("\\{\\{\\s*(\\w+)\\s*}}", "{$1}");
    }

    private LangfusePrompt readFallback(Resource fallbackTemplate) {
        try {
            return new LangfusePrompt(fallbackTemplate.getContentAsString(StandardCharsets.UTF_8), null, null);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read the fallback prompt " + fallbackTemplate, e);
        }
    }

    private record CachedPrompt(LangfusePrompt prompt, Instant fetchedAt) {
        boolean isFresh(Duration ttl) {
            return fetchedAt.plus(ttl).isAfter(Instant.now());
        }
    }
}
