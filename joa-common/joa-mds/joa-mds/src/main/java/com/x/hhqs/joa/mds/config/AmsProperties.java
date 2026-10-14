package com.x.hhqs.joa.mds.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "ams")
@RefreshScope
public class AmsProperties {

    private boolean useConfiguredCredentials = false;
    private String username;
    private String password;

    public boolean isUseConfiguredCredentials() {
        return useConfiguredCredentials;
    }

    public void setUseConfiguredCredentials(boolean useConfiguredCredentials) {
        this.useConfiguredCredentials = useConfiguredCredentials;
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
