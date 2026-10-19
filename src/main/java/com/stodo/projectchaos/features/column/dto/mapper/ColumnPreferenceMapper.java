package com.stodo.projectchaos.features.column.dto.mapper;

import com.stodo.projectchaos.features.column.dto.request.UpdateColumnPreferenceRequestDTO;
import com.stodo.projectchaos.features.column.dto.response.ColumnPreferenceResponseDTO;
import com.stodo.projectchaos.features.column.dto.service.ColumnPreference;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ColumnPreferenceMapper {
    ColumnPreferenceMapper INSTANCE = Mappers.getMapper(ColumnPreferenceMapper.class);

    ColumnPreferenceResponseDTO toColumnPreferenceResponseDTO(ColumnPreference columnPreference);

    ColumnPreference toColumnPreference(UpdateColumnPreferenceRequestDTO requestDTO);
}
