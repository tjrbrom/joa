package com.x.hhqs.joa.common.restclient.processor;

public interface InvalidTokenProcessor {
    void process(int statusCode, String responseText, boolean invalidTokenRetry);
}
