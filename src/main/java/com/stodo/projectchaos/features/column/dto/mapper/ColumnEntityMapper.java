package com.stodo.projectchaos.features.column.dto.mapper;

import com.stodo.projectchaos.model.entity.ColumnEntity;
import com.stodo.projectchaos.features.column.dto.service.Column;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ColumnEntityMapper {
    ColumnEntityMapper INSTANCE = Mappers.getMapper(ColumnEntityMapper.class);

    Column toColumn(ColumnEntity entity);
}