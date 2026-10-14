package com.x.hhqs.joa.common.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RT1CommandResponse {
    
    private String requestId;
    
    private String sessionId;
    
    private Long timestamp;
    
    private List<String> responses;
    
    public RT1CommandResponse() {}
    
    public RT1CommandResponse(String requestId, List<String> responses) {
        this.requestId = requestId;
        this.responses = responses;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getResponses() {
        return responses;
    }

    public void setResponses(List<String> responses) {
        this.responses = responses;
    }

}
