package com.x.hhqs.joa.sa.netconf;

import com.x.hhqs.joa.sa.api.SbiResponse;

public class NetconfResponse implements SbiResponse {
    private boolean error;
    private String errorMessage;
    private String requestType;
    private String response;

    public NetconfResponse(boolean error) {
        this.error = error;
    }

    public NetconfResponse(boolean error, String errorMessage) {
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public NetconfResponse(boolean error, String errorMessage, String requestType, String response) {
        this.error = error;
        this.errorMessage = errorMessage;
        this.requestType = requestType;
        this.response = response;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public boolean hasError() {
        return error;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
