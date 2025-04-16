package com.stodo.projectchaos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Entity not found");
        problem.setDetail(ex.getMessage());
        // on what endpoint the error occurred
        problem.setInstance(URI.create(request.getRequestURI()));

        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("entity", ex.getEntityType());
        problem.setProperty("identifiers", ex.getIdentifiers());

        return problem;
    }

    // Fallback
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Unexpected error");
        problem.setDetail(ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));

        problem.setProperty("timestamp", LocalDateTime.now());

        return problem;
    }

}
