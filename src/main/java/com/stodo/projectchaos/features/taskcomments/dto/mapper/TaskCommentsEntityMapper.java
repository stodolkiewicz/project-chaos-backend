package com.stodo.projectchaos.features.taskcomments.dto.mapper;

import com.stodo.projectchaos.features.taskcomments.dto.service.TaskComment;
import com.stodo.projectchaos.model.entity.TaskComments;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskCommentsEntityMapper {
    TaskCommentsEntityMapper INSTANCE = Mappers.getMapper(TaskCommentsEntityMapper.class);

    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "replyTo.id", target = "replyToId")
    TaskComment toTaskComment(TaskComments entity);
}