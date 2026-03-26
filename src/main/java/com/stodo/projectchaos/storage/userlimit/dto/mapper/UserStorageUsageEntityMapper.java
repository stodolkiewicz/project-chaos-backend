package com.stodo.projectchaos.storage.userlimit.dto.mapper;

import com.stodo.projectchaos.model.entity.UserStorageUsageEntity;
import com.stodo.projectchaos.storage.userlimit.dto.service.UserStorageUsage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserStorageUsageEntityMapper {
    UserStorageUsageEntityMapper INSTANCE = Mappers.getMapper(UserStorageUsageEntityMapper.class);

    @Mapping(source = "user.id", target = "userId")
    UserStorageUsage toUserStorageUsage(UserStorageUsageEntity entity);
}