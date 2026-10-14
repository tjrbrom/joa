package com.x.hhqs.joa.sa.exception;

public class TemplateServiceException extends SaException {
    static final long serialVersionUID = 3647546543L;

    public TemplateServiceException() {
        super();
    }

    public TemplateServiceException(String message) {
        super(message);
    }

    public TemplateServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateServiceException(Throwable cause) {
        super(cause);
    }

    protected TemplateServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
