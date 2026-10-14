package com.x.hhqs.joa.sa.exception;

public class QueryServiceException extends SaException {
    static final long serialVersionUID = 3647546541L;

    public QueryServiceException() {
        super();
    }

    public QueryServiceException(String message) {
        super(message);
    }

    public QueryServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryServiceException(Throwable cause) {
        super(cause);
    }

    protected QueryServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
