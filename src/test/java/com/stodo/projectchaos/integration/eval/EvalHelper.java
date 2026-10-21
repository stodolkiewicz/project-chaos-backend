package com.stodo.projectchaos.integration.eval;

import com.stodo.projectchaos.ai.tools.TaskProposalTools;
import com.stodo.projectchaos.features.project.ProjectService;
import com.stodo.projectchaos.features.project.dto.service.Project;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class EvalHelper {
    @Autowired
    ChatClient chatClient;

    @Value("classpath:/promptTemplates/system-prompt.st")
    private Resource systemPromptTemplate;

    @Autowired
    private ProjectService projectService;

    protected String ask(
            UUID projectId,
            UUID userId,
            String question,
            Sinks.Many<ServerSentEvent<Object>> events
    ) {
        Project project = projectService.findProjectById(projectId);
        return chatClient.prompt()
                .system(systemSpec -> systemSpec
                        .text(systemPromptTemplate)
                        .param("projectName", project.name())
                        .param("projectId", projectId)
                        .param("projectDescription", project.description())
                        .param("createdAt", project.createdDate())
                        .param("userId", userId)
                )
                .user(question)
                .toolContext(Map.of(
                        TaskProposalTools.EVENTS_KEY, events,
                        TaskProposalTools.PROJECT_ID_KEY, projectId
                )).call().content();
    }

    protected List<ServerSentEvent<Object>> collectSSEEvents(Sinks.Many<ServerSentEvent<Object>> events) {
        events.tryEmitComplete();
        return events.asFlux().collectList().block();
    }
}
