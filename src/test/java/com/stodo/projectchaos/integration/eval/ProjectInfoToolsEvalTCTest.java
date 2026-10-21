package com.stodo.projectchaos.integration.eval;

import com.stodo.projectchaos.ai.tools.ProjectInfoTools;
import com.stodo.projectchaos.ai.tools.TaskProposalTools;
import com.stodo.projectchaos.features.project.ProjectService;
import com.stodo.projectchaos.features.project.dto.service.Project;
import com.stodo.projectchaos.features.task.dto.service.BoardTask;
import com.stodo.projectchaos.integration.eval.config.EvalTCBase;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Sinks;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

public class ProjectInfoToolsEvalTCTest extends EvalTCBase {
    @Autowired
    ChatClient.Builder chatClientBuilder;

    @Autowired
    EvalHelper evalHelper;

    @Value("classpath:/promptTemplates/system-prompt.st")
    private Resource systemPromptTemplate;

    @MockitoSpyBean
    ProjectInfoTools projectInfoTools;

    /**
     * The {@link FactCheckingEvaluator} is designed to assess the factual accuracy of AI-generated responses
     * against provided context.
     *
     * <p>This evaluator helps detect and reduce hallucinations in AI outputs by verifying if a given statement (claim)
     * is logically supported by the provided context (document).
     */
    @Test
    void chat_shouldListHighPriorityBoardTasksWithAssignees() {
        // given
        String userQuestion = "Which tasks on the board have a high priority and who does them?";
        var factCheckingEvaluator = FactCheckingEvaluator.builder(chatClientBuilder).build();
        var relevancyEvaluator = RelevancyEvaluator.builder().chatClientBuilder(chatClientBuilder).build();

        UUID projectId = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
        UUID userId = UUID.fromString("96d9e09c-6cf0-4706-89ff-a12e1b8b8143");
        // TaskProposalTools sends task proposals to the frontend through this sink.
        // The replay sink keeps every event, so the test can read them after the call ends.
        Sinks.Many<ServerSentEvent<Object>> sink = Sinks.many().replay().all();

        List<BoardTask> returnedTasks = new ArrayList<>();
        doAnswer(invocation -> {
            List<BoardTask> result = (List<BoardTask>) invocation.callRealMethod();
            returnedTasks.addAll(result);
            return result;
        }).when(projectInfoTools).getAllTasksOnTheKanbanBoard(any());

        String answer = evalHelper.ask(projectId, userId, userQuestion, sink);

        EvaluationRequest evaluationRequest = new EvaluationRequest(
                userQuestion,
                // what was retrieved
                toDocuments(returnedTasks),
                answer);

        // when
        EvaluationResponse factCheckingEvaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);
        EvaluationResponse relevancyEvaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        List<ServerSentEvent<Object>> events = evalHelper.collectSSEEvents(sink);

        // then
        assertTrue(factCheckingEvaluationResponse.isPass());
        verify(projectInfoTools).getAllTasksOnTheKanbanBoard(projectId);
        assertThat(events).isEmpty();
        assertTrue(relevancyEvaluationResponse.isPass());
    }

    // Priority, column and assignee are optional, so a missing value is written as "none"
    private List<Document> toDocuments(List<BoardTask> tasks) {
        return tasks.stream()
                .map(task -> new Document(
                        "%s: priority %s, column %s, assignee %s".formatted(
                                task.getTitle(),
                                task.getPriority() == null ? "none" : task.getPriority().name(),
                                task.getColumn() == null ? "none" : task.getColumn().name(),
                                task.getAssignee() == null ? "none" : task.getAssignee().email()
                        )
                ))
                .toList();
    }
}
