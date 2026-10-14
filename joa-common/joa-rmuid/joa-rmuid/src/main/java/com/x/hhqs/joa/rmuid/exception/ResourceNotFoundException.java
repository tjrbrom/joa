package com.x.hhqs.joa.rmuid.exception;

public class ResourceNotFoundException extends JoaRmuidException {

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String s) {
        super(s);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 1L;

}
