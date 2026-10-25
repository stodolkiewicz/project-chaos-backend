package com.stodo.projectchaos.config;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.content.Content;
import org.springframework.ai.observation.ObservabilityHelper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * Adds the prompt and the completion of a model call to the span "chat gpt-5.4-mini".
 * Spring AI does not export this content by default, so Langfuse shows empty Input and Output.
 * Langfuse reads the Input from the span attribute "gen_ai.prompt"
 * and the Output from the span attribute "gen_ai.completion".
 * The filter is active only in the profile "dev", because prompts and completions can contain personal data.
 */
@Profile("dev")
@Component
public class ChatModelCompletionContentObservationFilter implements ObservationFilter {
    @Override
    public Observation.Context map(Observation.Context context) {
        // Only the ChatModel observation is the model call. Other observations stay unchanged.
        if (!(context instanceof ChatModelObservationContext chatModelObservationContext)) {
            return context;
        }
        var prompts = processPrompts(chatModelObservationContext);
        var completions = processCompletion(chatModelObservationContext);
        // The values are computed when the observation ends, so the filter sees the finished response.
        chatModelObservationContext.addHighCardinalityKeyValue(new KeyValue() {
            @Override
            public String getKey() {
                return "gen_ai.prompt";
            }
            @Override
            public String getValue() {
                return ObservabilityHelper.concatenateStrings(prompts);
            }
        });
        chatModelObservationContext.addHighCardinalityKeyValue(new KeyValue() {
            @Override
            public String getKey() {
                return "gen_ai.completion";
            }
            @Override
            public String getValue() {
                return ObservabilityHelper.concatenateStrings(completions);
            }
        });
        return chatModelObservationContext;
    }
    // Returns the text of all messages sent to the model: system, user, assistant and tool messages.
    private List<String> processPrompts(ChatModelObservationContext chatModelObservationContext) {
        return CollectionUtils.isEmpty((chatModelObservationContext.getRequest()).getInstructions()) ? List.of() : (chatModelObservationContext.getRequest()).getInstructions().stream().map(Content::getText).toList();
    }
    // Returns the texts of the model response. The list is empty when the response has no text,
    // for example when the model only calls a tool.
    private List<String> processCompletion(ChatModelObservationContext context) {
        if (context.getResponse() != null && (context.getResponse()).getResults() != null && !CollectionUtils.isEmpty((context.getResponse()).getResults())) {
            return !StringUtils.hasText((context.getResponse()).getResult().getOutput().getText()) ? List.of() : (context.getResponse()).getResults().stream().filter((generation) -> generation.getOutput() != null && StringUtils.hasText(generation.getOutput().getText())).map((generation) -> generation.getOutput().getText()).toList();
        } else {
            return List.of();
        }
    }
}