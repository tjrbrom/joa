package com.x.hhqs.joa.mds.emsclient.ams;

public class AmsRestClientConfig {
    private String serverName;

    private String baseUrl;

    private String username;

    private String password;

    public AmsRestClientConfig(String serverName, String baseUrl, String username, String password) {
        super();
        this.serverName = serverName;
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public static class Builder {
        private String serverName;
        private String baseUrl;
        private String username;
        private String password;

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder credential(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public AmsRestClientConfig build() {
            return new AmsRestClientConfig(serverName, baseUrl, username, password);
        }
    }

    public static Builder builder() {
        return new Builder();
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

}
