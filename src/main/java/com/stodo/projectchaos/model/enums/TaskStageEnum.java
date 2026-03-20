package com.stodo.projectchaos.model.enums;

public enum TaskStageEnum {
    BACKLOG("BACKLOG"),
    BOARD("BOARD"),
    DONE("DONE");

    private final String value;

    TaskStageEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}