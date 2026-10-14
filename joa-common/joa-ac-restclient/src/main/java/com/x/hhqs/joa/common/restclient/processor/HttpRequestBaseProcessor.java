package com.x.hhqs.joa.common.restclient.processor;

import org.apache.http.client.methods.HttpRequestBase;

@FunctionalInterface
public interface HttpRequestBaseProcessor<M extends HttpRequestBase> {

    void process(M method);
}
