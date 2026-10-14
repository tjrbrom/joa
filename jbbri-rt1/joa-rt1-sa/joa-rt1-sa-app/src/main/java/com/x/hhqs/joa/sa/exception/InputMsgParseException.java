package com.x.hhqs.joa.sa.exception;

public class InputMsgParseException extends CommandException {
    static final long serialVersionUID = 3846383645L;

    public InputMsgParseException() {
        super();
    }

    public InputMsgParseException(String message) {
        super(message);
    }

    public InputMsgParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public InputMsgParseException(Throwable cause) {
        super(cause);
    }
}
