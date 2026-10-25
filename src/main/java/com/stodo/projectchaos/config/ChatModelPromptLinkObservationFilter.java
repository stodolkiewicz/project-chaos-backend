package com.stodo.projectchaos.config;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.stereotype.Component;

/**
 * Links the Langfuse generation to a prompt version managed in Langfuse.
 * Langfuse reads the prompt name from the span attribute "langfuse.observation.prompt.name"
 * and the prompt version from the span attribute "langfuse.observation.prompt.version".
 * Both attributes work only on a generation, so this filter adds them to the span "chat gpt-5.4-mini".
 * The application passes the values as metadata of the system message,
 * because the ChatModelObservationContext has no access to the params of the ChatClient.
 */
@Component
public class ChatModelPromptLinkObservationFilter implements ObservationFilter {
    public static final String PROMPT_NAME = "langfusePromptName";
    public static final String PROMPT_VERSION = "langfusePromptVersion";

    @Override
    public Observation.Context map(Observation.Context context) {
        // Only the ChatModel observation is the generation. Other observations stay unchanged.
        if (!(context instanceof ChatModelObservationContext chatModelObservationContext)) {
            return context;
        }
        for (Message message : chatModelObservationContext.getRequest().getInstructions()) {
            Object name = message.getMetadata().get(PROMPT_NAME);
            Object version = message.getMetadata().get(PROMPT_VERSION);
            // Calls without prompt metadata (for example the title generation) stay unlinked.
            if (name != null && version != null) {
                chatModelObservationContext.addHighCardinalityKeyValue(
                        KeyValue.of("langfuse.observation.prompt.name", name.toString()));
                chatModelObservationContext.addHighCardinalityKeyValue(
                        KeyValue.of("langfuse.observation.prompt.version", version.toString()));
                break;
            }
        }
        return chatModelObservationContext;
    }
}
