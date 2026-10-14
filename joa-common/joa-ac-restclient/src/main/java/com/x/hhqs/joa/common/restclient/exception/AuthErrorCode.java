package com.x.hhqs.joa.common.restclient.exception;

import com.x.hhqs.joa.common.restclient.ErrorResponse;

public enum AuthErrorCode {
    LOGOUT(200, "Logout"),
    CREDENTIAL_MISSING(401, "username or password missing"),
    INVALID_CREDENTIAL(401, "Invalid credential"),
    MISSING_REFRESH_TOKEN(400, "Missing refresh token"),
    INVALID_REFRESH_TOKEN(401, "Refresh token is not valid anymore"),
    MISSING_ACCESS_TOKEN(400, "Access token is missing"),
    INVALID_ACCESS_TOKEN(401, "Access token is not valid anymore"),
    MISSING_AUTHORIZATION_HEADER(401, "Authorization header is missing"),
    SERVER_NOT_AVAILABLE(500, "Keycloak is not available"),
    ACCESS_DENIED_INVALID_TOKEN(500, "Access denied, token is not valid"),
    OPERATION_NOT_AUTHORIZED(403, "Operation not authorized");

    private final int m_errorCode;
    private final String m_errorMsg;

    AuthErrorCode(int errorCode, String errorMsg) {
        m_errorCode = errorCode;
        m_errorMsg = errorMsg;
    }

    public int getErrorCode() {
        return m_errorCode;
    }

    public String getAuthenticateHeader() {
        if (m_errorCode == 401) {
            return "WWW-Authenticate";
        }
        return null;
    }

    public ErrorResponse toResponseEntity() {
        return new ErrorResponse(m_errorMsg);
    }
}
