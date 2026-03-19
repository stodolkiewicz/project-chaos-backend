package com.stodo.projectchaos.ai.tools;

import com.stodo.projectchaos.features.user.UserService;
import com.stodo.projectchaos.features.user.dto.service.User;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserInfoTools {

    private final UserService userService;

    public CurrentUserInfoTools(UserService userService) {
        this.userService = userService;
    }

    @Tool(description = "Get information about the user you are talking with")
    public User getCurrentUserInfo(
            @ToolParam(description = "technical UUID identifier of the user.", required = true) UUID userId
    ) {
        return userService.getUserById(userId);
    }
}
