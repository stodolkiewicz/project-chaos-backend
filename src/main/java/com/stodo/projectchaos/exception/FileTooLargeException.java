package com.stodo.projectchaos.exception;

public class FileTooLargeException extends RuntimeException {

    public FileTooLargeException(String filename, long maxFileSize) {
        super(String.format(
                "File '%s' exceeded max file size of %d bytes",
                filename,
                maxFileSize
        ));
    }
}
