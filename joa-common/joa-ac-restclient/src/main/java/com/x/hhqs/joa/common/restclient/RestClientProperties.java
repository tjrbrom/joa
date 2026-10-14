package com.x.hhqs.joa.common.restclient;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "core.rest")
public class RestClientProperties {
    /**
     * Base url of core-ac, for example:
     * https://10.182.105.211/automation-core-ac
     * @mandatory
     */
    private String baseUrl;

    /**
     * Username of core-ac user, for example: adminuser
     * @mandatory
     */
    private String username;

    /**
     * Password of core-ac user, for example: password
     * @mandatory
     */
    private String password;

    /**
     * Token life time. If current access_token's life greater than this, access_token will be refreshed.
     * Default value: 300,000ms (5 minutes)
     * @optional
     */
    private long tokenRefreshTime;

    /**
     * Connection timeout of request.
     * Default value: 15000ms (15 seconds)
     * @optional
     */
    private int connectTimeout;

    /**
     * Read timeout of request
     * Default value: 300000ms (300 seconds)
     * @optional
     */
    private int readTimeout;

    /**
     * Assigns maximum total connection value.
     * Default value: 20
     * @optional
     */
    private int maxTotalConnections;

    /**
     * Assigns maximum connection per route value.
     * Default value: 20
     * @optional
     */
    private int maxConnectionsPerRoute;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getTokenRefreshTime() {
        return tokenRefreshTime;
    }

    public void setTokenRefreshTime(long tokenRefreshTime) {
        this.tokenRefreshTime = tokenRefreshTime;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }
}
