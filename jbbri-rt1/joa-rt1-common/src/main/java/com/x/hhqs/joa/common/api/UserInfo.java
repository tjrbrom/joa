package com.x.hhqs.joa.common.api;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {

    private String username;

    private String sessionId;

    private Set<String> paps;

    public UserInfo() {}

    public UserInfo(String username, String sessionId) {
        super();
        this.username = username;
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getPaps() {
        return paps;
    }

    public void setPaps(Set<String> paps) {
        this.paps = paps;
    }

}
