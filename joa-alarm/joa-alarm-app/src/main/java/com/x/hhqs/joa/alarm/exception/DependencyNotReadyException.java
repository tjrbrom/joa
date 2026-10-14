package com.x.hhqs.joa.alarm.exception;

public class DependencyNotReadyException extends RuntimeException {

    public DependencyNotReadyException(String message) {
        super(message);
    }

    public DependencyNotReadyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DependencyNotReadyException(Throwable cause) {
        super(cause);
    }
}