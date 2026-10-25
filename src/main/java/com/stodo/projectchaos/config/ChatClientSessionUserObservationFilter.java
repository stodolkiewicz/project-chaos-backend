package com.stodo.projectchaos.config;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.springframework.ai.chat.client.observation.ChatClientObservationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * Assigns the Langfuse traces to a Langfuse session and to a Langfuse user.
 * Langfuse reads the session id from the span attribute "langfuse.session.id"
 * and the user id from the span attribute "langfuse.user.id".
 * Spring AI does not set these attributes, so this filter adds them to the root span "spring_ai chat_client".
 * The session id is the conversation id passed to the ChatClient as CONVERSATION_ID.
 * The user id is passed to the ChatClient as the advisor param USER_ID.
 */
@Component
public class ChatClientSessionUserObservationFilter implements ObservationFilter {
    public static final String USER_ID = "userId";

    @Override
    public Observation.Context map(Observation.Context context) {
        // Only the ChatClient observation is the root span. Other observations stay unchanged.
        if (!(context instanceof ChatClientObservationContext chatClientObservationContext)) {
            return context;
        }
        Map<String, Object> requestContext = chatClientObservationContext.getRequest().context();
        // Calls without a conversation id or a user id (for example the title generation) get no session or user.
        addIfPresent(chatClientObservationContext, "langfuse.session.id", requestContext.get(CONVERSATION_ID));
        addIfPresent(chatClientObservationContext, "langfuse.user.id", requestContext.get(USER_ID));
        return chatClientObservationContext;
    }

    private void addIfPresent(Observation.Context context, String key, Object value) {
        if (value != null) {
            context.addHighCardinalityKeyValue(KeyValue.of(key, value.toString()));
        }
    }
}
