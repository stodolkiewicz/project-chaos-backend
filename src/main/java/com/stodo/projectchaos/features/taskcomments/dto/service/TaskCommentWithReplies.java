package com.stodo.projectchaos.features.taskcomments.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCommentWithReplies {
    private UUID id;
    private UUID taskId;
    private UUID authorId;
    private String content;
    private UUID replyToId;
    private Instant createdDate;
    private String lastModifiedBy;
    private Instant lastModifiedDate;

    @Builder.Default
    private List<TaskCommentWithReplies> replies = new ArrayList<>();
}