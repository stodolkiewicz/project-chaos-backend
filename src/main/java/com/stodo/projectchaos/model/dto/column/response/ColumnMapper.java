package com.stodo.projectchaos.model.dto.column.response;

import com.stodo.projectchaos.model.entity.ColumnEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ColumnMapper {
    ColumnMapper INSTANCE = Mappers.getMapper(ColumnMapper.class);

    ColumnResponseDTO toColumnResponseDTO(ColumnEntity column);
} 