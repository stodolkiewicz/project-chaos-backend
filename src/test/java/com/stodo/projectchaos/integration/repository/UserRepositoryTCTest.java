package com.stodo.projectchaos.integration.repository;

import com.stodo.projectchaos.integration.repository.config.TestContainersBase;
import com.stodo.projectchaos.integration.repository.config.TestcontainersPostgresConfiguration;
import com.stodo.projectchaos.integration.repository.helper.TcHelper;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.ProjectUsersEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.repository.ProjectRepository;
import com.stodo.projectchaos.repository.ProjectUsersRepository;
import com.stodo.projectchaos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import({TestcontainersPostgresConfiguration.class, TcHelper.class})
class UserRepositoryTCTest extends TestContainersBase {

    @Autowired
    TcHelper tcHelper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectUsersRepository projectUsersRepository;

    @Autowired
    ProjectRepository projectRepository;

    //    Description of query being tested:
    //      Update all users - set their new default project to the max ID project (=random) from those they have access
    //      to (but not the deleted project), but only for users who:
    //            1. Currently have the deleted project as default
    //            2. And simultaneously have access to some other project (i.e., have an alternative)
    @Test
    public void batchUpdateDefaultProjectsForUsersWithAlternatives_shouldSwitchToAlternative_onlyWhenDeletedProjectWasUsersDefault() {
        // given
        UUID projectBeingDeletedId = tcHelper.createProject("projectBeingDeleted");
        UUID otherProjectId = tcHelper.createProject("otherProject");

        String emailUserOnlyProject = "emailUserOnlyProject@gmail.com";
        String emailHasAltDeletedSameAsDefault = "emailHasAltDeletedSameAsDefault@gmail.com";
        String emailHasAltDeletedDifferentThanDefault = "emailHasAltDeletedDifferentThanDefault@gmail.com";

        // SHOULD NOT BE AFFECTED
        // user who is assigned to the deleted project and it is his only project == is his default project
        tcHelper.createUserWithDefaultProject(emailUserOnlyProject, projectBeingDeletedId);
        tcHelper.assignUserToProject(emailUserOnlyProject, projectBeingDeletedId);

        // SHOULD BE AFFECTED
        // user who is assigned to the deleted project, has an alternative, his default project == project deleted
        tcHelper.createUserWithDefaultProject(emailHasAltDeletedSameAsDefault, projectBeingDeletedId);
        tcHelper.assignUserToProject(emailHasAltDeletedSameAsDefault, projectBeingDeletedId);
        tcHelper.assignUserToProject(emailHasAltDeletedSameAsDefault, otherProjectId);

        // SHOULD NOT BE AFFECTED
        // user who is assigned to the deleted project, has an alternative, his default project != project deleted
        tcHelper.createUserWithDefaultProject(emailHasAltDeletedDifferentThanDefault, otherProjectId);
        tcHelper.assignUserToProject(emailHasAltDeletedDifferentThanDefault, projectBeingDeletedId);
        tcHelper.assignUserToProject(emailHasAltDeletedDifferentThanDefault, otherProjectId);

        List<UserEntity> allUsersBefore = userRepository.findAll();
        List<ProjectUsersEntity> allProjectUsersBefore = projectUsersRepository.findAll();
        List<ProjectEntity> allProjectsBefore = projectRepository.findAll();

        // when
        int modifiedRowsCount = userRepository.batchUpdateDefaultProjectsForUsersWithAlternatives(projectBeingDeletedId);

        // then
        UserEntity userOnlyProject = userRepository.findByEmail(emailUserOnlyProject).get();
        UserEntity userHasAltDeletedSameAsDefault = userRepository.findByEmail(emailHasAltDeletedSameAsDefault).get();
        UserEntity userHasAltDeletedDifferentThanDefault = userRepository.findByEmail(emailHasAltDeletedDifferentThanDefault).get();

        assertThat(modifiedRowsCount).isEqualTo(1);
        assertThat(userOnlyProject.getProject().getId()).isEqualTo(projectBeingDeletedId);
        // projectId changed for this user
        assertThat(userHasAltDeletedSameAsDefault.getProject().getId()).isEqualTo(otherProjectId);
        assertThat(userHasAltDeletedDifferentThanDefault.getProject().getId()).isEqualTo(otherProjectId);
    }
}
