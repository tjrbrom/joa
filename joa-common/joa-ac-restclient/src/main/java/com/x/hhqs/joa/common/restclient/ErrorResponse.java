package com.x.hhqs.joa.common.restclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    @JsonProperty("error")
    private String m_error;

    public ErrorResponse() {
    }

    public ErrorResponse(String error) {
        this.m_error = error;
    }

    public String getError() {
        return m_error;
    }

    public void setError(String error) {
        this.m_error = error;
    }
}
