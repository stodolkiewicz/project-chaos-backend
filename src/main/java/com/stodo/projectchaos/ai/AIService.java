package com.stodo.projectchaos.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class AIService {

    private final ChatClient chatClient;

    public AIService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public Flux<String> talk(String question, String conversationId) {
        return chatClient.prompt()
                .system("You are helpful adviser. Your answers are cheerful, but rather on the concise side.")
                .user(question)
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, conversationId))
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    // Logujemy tylko wtedy, gdy faktycznie dostaliśmy dane o zużyciu
                    System.out.println(response);
                    var usage = response.getMetadata().getUsage();
                    if (usage != null && usage.getTotalTokens() > 0) {
                        System.out.println("FINALNE ZUŻYCIE: " + usage.getTotalTokens());
                    }
                })
                .map(response -> {
                    // Bezpieczne wyciąganie tekstu - sprawdzamy czy result nie jest null
                    if (response.getResult() != null && response.getResult().getOutput() != null) {
                        String text = response.getResult().getOutput().getText();
                        return text != null ? text : "";
                    }
                    return "";
                })
                .filter(text -> !text.isEmpty()); // Odrzucamy puste stringi z ostatniego pakietu
    }

}
