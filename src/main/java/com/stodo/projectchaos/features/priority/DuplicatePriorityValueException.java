package com.stodo.projectchaos.features.priority;

public class DuplicatePriorityValueException extends RuntimeException {
    public DuplicatePriorityValueException(short priorityValue) {
        super(String.format("More than one priority has value %d. Every priority needs a different value.", priorityValue));
    }
}
