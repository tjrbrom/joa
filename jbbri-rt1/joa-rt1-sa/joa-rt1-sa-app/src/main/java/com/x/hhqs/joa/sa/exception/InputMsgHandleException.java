package com.x.hhqs.joa.sa.exception;

public class InputMsgHandleException extends CommandException {
    static final long serialVersionUID = 3846383826L;

    public InputMsgHandleException() {
    }

    public InputMsgHandleException(String message) {
        super(message);
    }

    public InputMsgHandleException(String message, Throwable cause) {
        super(message, cause);
    }

    public InputMsgHandleException(Throwable cause) {
        super(cause);
    }
}
