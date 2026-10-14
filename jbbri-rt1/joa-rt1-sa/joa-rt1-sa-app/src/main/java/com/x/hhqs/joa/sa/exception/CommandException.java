package com.x.hhqs.joa.sa.exception;

public class CommandException extends Exception {
    static final long serialVersionUID = 3647546528L;
    public CommandException() {
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandException(Throwable cause) {
        super(cause);
    }
}
