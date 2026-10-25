package com.stodo.projectchaos.ai.features.prompt.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The response of GET /api/public/v2/prompts/{promptName} of the Langfuse API.
 * The field "prompt" is a string for the prompt type "text".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LangfusePromptResponseDTO(
        String name,
        String type,
        String prompt,
        int version
) {
}
