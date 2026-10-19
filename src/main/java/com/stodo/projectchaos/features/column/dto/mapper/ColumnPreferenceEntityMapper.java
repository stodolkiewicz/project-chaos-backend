package com.stodo.projectchaos.features.column.dto.mapper;

import com.stodo.projectchaos.features.column.dto.service.ColumnPreference;
import com.stodo.projectchaos.model.entity.UserColumnPreferenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ColumnPreferenceEntityMapper {
    ColumnPreferenceEntityMapper INSTANCE = Mappers.getMapper(ColumnPreferenceEntityMapper.class);

    @Mapping(target = "columnId", source = "id.column")
    ColumnPreference toColumnPreference(UserColumnPreferenceEntity entity);
}
