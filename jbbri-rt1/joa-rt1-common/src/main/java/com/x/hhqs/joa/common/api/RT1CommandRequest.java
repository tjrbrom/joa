package com.x.hhqs.joa.common.api;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RT1CommandRequest {

    private String version = "v1";

    private String command;

    private UserInfo userInfo;

    private String requestId;

    private Long timestamp;

    private Map<String, Object> extensions;

    public RT1CommandRequest() {}

    public RT1CommandRequest(String command, UserInfo userInfo, String requestId) {
        this.command = command;
        this.userInfo = userInfo;
        this.requestId = requestId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }
}
