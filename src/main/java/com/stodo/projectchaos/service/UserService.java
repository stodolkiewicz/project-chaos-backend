package com.stodo.projectchaos.service;

import com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.repository.UserRepository;
import com.stodo.projectchaos.model.dto.user.projectusers.ProjectUserMapper;
import com.stodo.projectchaos.model.dto.user.projectusers.response.ProjectUsersResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public ProjectUsersResponseDTO findProjectUsersByProjectId(UUID projectId) {
        List<ProjectUserQueryResponseDTO> users = userRepository.findProjectUsersByProjectId(projectId);
        return ProjectUserMapper.INSTANCE.toProjectUsersResponseDTO(users);
    }
}
