package com.stodo.projectchaos.ai.features.prompt.dto.service;

/**
 * A text prompt in the template syntax of Spring AI, for example "{projectName}".
 * The name and the version identify the prompt in Langfuse.
 * Both are null when the text comes from the fallback file.
 */
public record LangfusePrompt(
        String text,
        String name,
        Integer version
) {
    public boolean isFromLangfuse() {
        return name != null && version != null;
    }
}
