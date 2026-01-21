package com.helpdesk.status.exception;

public class StatusServiceException extends RuntimeException {
    public StatusServiceException(String message) {
        super(message);
    }

    public StatusServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}