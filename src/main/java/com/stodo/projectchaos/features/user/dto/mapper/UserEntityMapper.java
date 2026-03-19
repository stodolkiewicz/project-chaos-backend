package com.stodo.projectchaos.features.user.dto.mapper;

import com.stodo.projectchaos.features.user.dto.service.User;
import com.stodo.projectchaos.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserEntityMapper {
    UserEntityMapper INSTANCE = Mappers.getMapper(UserEntityMapper.class);
    
    User toUser(UserEntity entity);
}