package com.x.hhqs.joa.common.restclient;

public class TokenResponseWrapper {
    private TokenResponse m_tokenResponse;

    /**
     * in milliseconds
     */
    private long m_expiresAt;

    /**
     * in milliseconds
     */
    private long m_refreshExpiresAt;

    public TokenResponseWrapper(TokenResponse tokenResponse) {
        if (tokenResponse == null) {
            throw new IllegalArgumentException("Token response cannot be null");
        }
        this.m_tokenResponse = tokenResponse;
        long currentTimeMillis = System.currentTimeMillis();

        m_expiresAt = currentTimeMillis + tokenResponse.getExpiresIn() * 1000;
        m_refreshExpiresAt = currentTimeMillis + tokenResponse.getRefreshExpiresIn() * 1000;
    }

    /**
     *
     * @return access token expiration point in milliseconds
     */
    public long getExpiresAt() {
        return m_expiresAt;
    }

    // UT
    public void setExpiresAt(long expiresAt) {
        this.m_expiresAt = expiresAt;
    }

    /**
     *
     * @return refresh token expiration point in milliseconds
     */
    public long getRefreshExpiresAt() {
        return m_refreshExpiresAt;
    }

    // UT
    public void setRefreshExpiresAt(long refreshExpiresAt) {
        this.m_refreshExpiresAt = refreshExpiresAt;
    }

    public TokenResponse getTokenResponse() {
        return m_tokenResponse;
    }

    public void setTokenResponse(TokenResponse tokenResponse) {
        this.m_tokenResponse = tokenResponse;
    }

    @Override
    public String toString() {
        return "TokenResponseWrapper [m_tokenResponse=" + m_tokenResponse + ", m_expiresAt=" + m_expiresAt
                + "ms, m_refreshExpiresAt=" + m_refreshExpiresAt + "ms]";
    }
}
