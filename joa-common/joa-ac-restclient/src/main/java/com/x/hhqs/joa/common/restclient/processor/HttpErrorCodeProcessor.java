package com.x.hhqs.joa.common.restclient.processor;

public interface HttpErrorCodeProcessor {
    Object process(int statusCode, String responseText) throws Exception;
}
