package com.x.hhqs.joa.common.restclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResponse {
    @JsonProperty("access_token")
    private String m_accessToken;

    @JsonProperty("refresh_token")
    private String m_refreshToken;

    @JsonProperty("expires_in")
    private long m_expiresIn;

    @JsonProperty("refresh_expires_in")
    private long m_refreshExpiresIn;

    @JsonProperty("token_type")
    private String m_tokenType;

    public TokenResponse() {
    }

    public TokenResponse(String rawTokenString, String refreshTokenString, long expiresIn, long refreshExpiresIn) {
        m_accessToken = rawTokenString;
        m_refreshToken = refreshTokenString;
        m_expiresIn = expiresIn;
        m_refreshExpiresIn = refreshExpiresIn;
    }

    public String getAccessToken() {
        return m_accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.m_accessToken = accessToken;
    }

    public String getRefreshToken() {
        return m_refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.m_refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return m_expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.m_expiresIn = expiresIn;
    }

    public long getRefreshExpiresIn() {
        return m_refreshExpiresIn;
    }

    public void setRefreshExpiresIn(long refreshExpiresIn) {
        this.m_refreshExpiresIn = refreshExpiresIn;
    }

    public String getTokenType() {
        return m_tokenType;
    }

    public void setTokenType(String tokenType) {
        m_tokenType = tokenType;
    }

    @Override
    public String toString() {
        return "TokenResponse [m_accessToken=" + m_accessToken + ", m_refreshToken=" + m_refreshToken + ", m_expiresIn="
                + m_expiresIn + "ms , m_refreshExpiresIn=" + m_refreshExpiresIn + "ms, m_tokenType=" + m_tokenType + "]";
    }
}
