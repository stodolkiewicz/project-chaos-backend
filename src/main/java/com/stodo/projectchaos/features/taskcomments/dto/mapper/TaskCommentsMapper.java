package com.stodo.projectchaos.features.taskcomments.dto.mapper;

import com.stodo.projectchaos.features.taskcomments.dto.response.TaskCommentResponseDTO;
import com.stodo.projectchaos.features.taskcomments.dto.response.TaskCommentWithRepliesResponseDTO;
import com.stodo.projectchaos.features.taskcomments.dto.service.TaskComment;
import com.stodo.projectchaos.features.taskcomments.dto.service.TaskCommentWithReplies;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TaskCommentsMapper {
    TaskCommentsMapper INSTANCE = Mappers.getMapper(TaskCommentsMapper.class);

    TaskCommentResponseDTO toTaskCommentResponseDTO(TaskComment taskComment);

    List<TaskCommentWithReplies> toTaskCommentWithReplies(List<TaskComment> taskComments);

    TaskCommentWithRepliesResponseDTO toTaskCommentWithRepliesResponseDTO(TaskCommentWithReplies taskCommentWithReplies);
}