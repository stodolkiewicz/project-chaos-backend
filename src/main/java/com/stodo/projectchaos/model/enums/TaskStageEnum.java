package com.stodo.projectchaos.model.enums;

public enum TaskStageEnum {
    BACKLOG("BACKLOG"),
    BOARD("BOARD"),
    ARCHIVED("ARCHIVED");

    private final String value;

    TaskStageEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}