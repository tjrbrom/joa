package com.x.hhqs.joa.alarm.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakTokenVo implements Serializable {

    @JsonAlias("access_token")
    private String accessToken;

    @JsonAlias("expires_in")
    private Long expiresInSecond;

    @JsonAlias("refresh_expires_in")
    private Long refreshExpiresInSecond;

    @JsonAlias("refresh_token")
    private String refreshToken;

    @JsonAlias("token_type")
    private String tokenType;

    @JsonAlias("not-before-policy")
    private long notBeforePolicy;

    @JsonAlias("session_state")
    private String sessionState;

    @JsonAlias("scope")
    private String scope;

    private List<String> roles;
}
