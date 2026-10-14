package com.x.hhqs.joa.sa.exception;

public class SaException extends Exception {
    static final long serialVersionUID = 3647546539L;
    public SaException() {
        super();
    }

    public SaException(String message) {
        super(message);
    }

    public SaException(String message, Throwable cause) {
        super(message, cause);
    }

    public SaException(Throwable cause) {
        super(cause);
    }

    protected SaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
