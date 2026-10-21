package com.stodo.projectchaos.integration.eval;

import com.stodo.projectchaos.ai.tools.ProjectInfoTools;
import com.stodo.projectchaos.ai.tools.RequestCreateTaskEventDTO;
import com.stodo.projectchaos.ai.tools.RequestDeleteTaskEventDTO;
import com.stodo.projectchaos.ai.tools.TaskProposalTools;
import com.stodo.projectchaos.integration.eval.config.EvalTCBase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class TaskProposalToolsEvalTCTest extends EvalTCBase {
    @Autowired
    ChatClient.Builder chatClientBuilder;

    @Autowired
    EvalHelper evalHelper;

    @Value("classpath:/promptTemplates/system-prompt.st")
    private Resource systemPromptTemplate;

    @MockitoSpyBean
    TaskProposalTools taskProposalTools;

    @MockitoSpyBean
    ProjectInfoTools projectInfoTools;

    @Test
    void chat_whenAskedToCreateATask_shouldSendASSEEventWithTaskProposal() {
        // given
        String userQuestion = "Pls create a task to manually test the pull request #5234 on dev environment";
        UUID projectId = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
        UUID userId = UUID.fromString("96d9e09c-6cf0-4706-89ff-a12e1b8b8143");

        // TaskProposalTools sends task proposals to the frontend through this sink.
        // The replay sink keeps every event, so the test can read them after the call ends.
        Sinks.Many<ServerSentEvent<Object>> sink = Sinks.many().replay().all();

        // when
        String answer = evalHelper.ask(projectId, userId, userQuestion, sink);

        List<ServerSentEvent<Object>> events = evalHelper.collectSSEEvents(sink);
        Verdict verdict = judgeAssistantDidNotSayItCreatedOrDeletedAnything(userQuestion, answer);

        // then
        assertThat(events.size()).isEqualTo(1);
        assertEquals("RequestCreateTask", events.getFirst().event());
        assertNotNull(((RequestCreateTaskEventDTO) events.getFirst().data()).title());
        verify(taskProposalTools).createTask(any(), any(), any());
        assertTrue(verdict.decision);
    }

    @Test
    void chat_whenAskedToDeleteAnExistingTask_shouldCheckTaskIdAndSendASSEEventWithTaskDeletion() {
        // given
        String userQuestion = "Pls delete task concerning bug 42";
        UUID projectId = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
        UUID userId = UUID.fromString("96d9e09c-6cf0-4706-89ff-a12e1b8b8143");
        UUID taskRequestedToBeDeletedId = UUID.fromString("adbd6529-1b3d-4ae2-a6fb-63fce48f133d");

        // TaskProposalTools sends task proposals to the frontend through this sink.
        // The replay sink keeps every event, so the test can read them after the call ends.
        Sinks.Many<ServerSentEvent<Object>> sink = Sinks.many().replay().all();

        // when
        String answer = evalHelper.ask(projectId, userId, userQuestion, sink);

        List<ServerSentEvent<Object>> events = evalHelper.collectSSEEvents(sink);
        Verdict verdict = judgeAssistantDidNotSayItCreatedOrDeletedAnything(userQuestion, answer);

        // then
        assertThat(events.size()).isEqualTo(1);
        assertEquals("RequestDeleteTask", events.getFirst().event());
        assertNotNull(((RequestDeleteTaskEventDTO) events.getFirst().data()).title());
        verify(projectInfoTools).getAllProjectTasks(projectId);
        verify(taskProposalTools).deleteTask(eq(taskRequestedToBeDeletedId), any());
        assertTrue(verdict.decision);
    }

    record Verdict(boolean decision, String reason){};

    private Verdict judgeAssistantDidNotSayItCreatedOrDeletedAnything(String userQuestion, String answer) {
        return chatClientBuilder.build()
                .prompt()
                .user("""
                        A Kanban assistant is only allow to show task creation form
                        or task deletion form. He may never say that he created or deleted a task.
                         
                        Decide whether the assistant follows the aforementioned rules.
                        
                        Return your decision and short reason.

                        Question:
                        %s

                        Response:
                        %s
                        
                        """.formatted(
                        userQuestion,
                        answer
                ))
                .call()
                .entity(Verdict.class);
    }

}
