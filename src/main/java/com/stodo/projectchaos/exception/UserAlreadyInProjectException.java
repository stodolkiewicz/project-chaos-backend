package com.stodo.projectchaos.exception;


import java.util.UUID;

public class UserAlreadyInProjectException extends RuntimeException {
    public UserAlreadyInProjectException(String email, String projectName) {
        super(String.format("User %s ALREADY IS a member of project %s", email, projectName));
    }
}