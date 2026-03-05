package com.stodo.projectchaos.features.projectuser;

public class LastAdminCannotLeaveProjectException extends RuntimeException {
    public LastAdminCannotLeaveProjectException() {
        super("Last admin cannot leave the project. Please assign another admin first or delete the project.");
    }
}