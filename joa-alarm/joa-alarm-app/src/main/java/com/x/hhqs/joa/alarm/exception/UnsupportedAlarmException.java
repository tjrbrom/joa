package com.x.hhqs.joa.alarm.exception;

/**
 * Exception thrown when an alarm message cannot be consumed and should be skipped.
 */
public class UnsupportedAlarmException extends RuntimeException {

    public UnsupportedAlarmException(String message) {
        super(message);
    }

    public UnsupportedAlarmException(String message, Throwable cause) {
        super(message, cause);
    }
}