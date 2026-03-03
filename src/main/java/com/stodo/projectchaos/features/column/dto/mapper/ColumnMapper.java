package com.stodo.projectchaos.features.column.dto.mapper;

import com.stodo.projectchaos.features.column.dto.response.ColumnResponseDTO;
import com.stodo.projectchaos.model.entity.ColumnEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ColumnMapper {
    ColumnMapper INSTANCE = Mappers.getMapper(ColumnMapper.class);

    ColumnResponseDTO toColumnResponseDTO(ColumnEntity column);
} 