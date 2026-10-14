package com.x.hhqs.joa.common.restclient.exception;

public class AuthException extends RuntimeException {
    private final AuthErrorCode m_errorCode;

    public AuthException(AuthErrorCode errorCode) {
        m_errorCode = errorCode;
    }

    public AuthErrorCode getErrorCode() {
        return m_errorCode;
    }
}
