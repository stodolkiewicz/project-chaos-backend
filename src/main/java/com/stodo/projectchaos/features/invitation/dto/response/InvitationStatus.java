package com.stodo.projectchaos.features.invitation.dto.response;

public enum InvitationStatus {
    INVITED("INVITED"),
    ADDED("ADDED");

    private final String status;

    InvitationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
