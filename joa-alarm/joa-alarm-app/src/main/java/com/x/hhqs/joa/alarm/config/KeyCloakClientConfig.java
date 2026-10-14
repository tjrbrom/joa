package com.x.hhqs.joa.alarm.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component("keycloakClientConfig")
@ConfigurationProperties(prefix = "joa-alarm.keycloak")
public class KeyCloakClientConfig {

    private boolean enabled = false;

    private String authServerUrl;

    private String realm;

    private String resource;

    private credentials credentials;

    private String userInfo;

    private String token;

    private String endSession;

    private Long httpTimeout = -1L;

    //
    private String defaultUsername;

    private String defaultPassword;

    @Getter
    @Setter
    public static class credentials {
        private String secret;
    }
}
