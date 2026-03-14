package com.stodo.projectchaos.exception;

public class TooManyAIRequestsException extends RuntimeException {

    public TooManyAIRequestsException(int timeWindowInMinutes, int numberOfRequestsInTimeWindowLimit) {
        super(String.format(
                "You have exceeded your AI request limit. " +
                "You can send %d requests every %d minutes. " +
                "Access will be restored gradually as your older requests expire.",
                numberOfRequestsInTimeWindowLimit,
                timeWindowInMinutes)
        );
    }
}
