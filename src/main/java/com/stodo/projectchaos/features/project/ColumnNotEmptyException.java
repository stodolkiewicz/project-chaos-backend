package com.stodo.projectchaos.features.project;

public class ColumnNotEmptyException extends RuntimeException {
    public ColumnNotEmptyException(String columnName) {
        super(String.format("Column \"%s\" still has tasks. Move them first.", columnName));
    }
}
