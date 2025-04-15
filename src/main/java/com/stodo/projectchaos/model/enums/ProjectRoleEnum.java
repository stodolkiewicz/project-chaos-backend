package com.stodo.projectchaos.model.enums;

public enum ProjectRoleEnum {
    ADMIN("ADMIN"),
    MEMBER("MEMBER"),
    VIEWER("VIEWER");

    private final String role;

    ProjectRoleEnum(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
