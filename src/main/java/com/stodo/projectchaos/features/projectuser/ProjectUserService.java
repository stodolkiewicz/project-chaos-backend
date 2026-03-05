package com.stodo.projectchaos.features.projectuser;

import com.stodo.projectchaos.features.projectuser.dto.mapper.ProjectUserMapper;
import com.stodo.projectchaos.features.projectuser.dto.service.ProjectUser;
import com.stodo.projectchaos.features.user.dto.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.features.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectUserService {

    private final UserRepository userRepository;

    public List<ProjectUser> findProjectUsersByProjectId(UUID projectId) {
        List<ProjectUserQueryResponseDTO> projectUsersQueryResponse = userRepository.findProjectUsersByProjectId(projectId);
        
        return ProjectUserMapper.INSTANCE.toListOfProjectUsers(projectUsersQueryResponse);
    }
}