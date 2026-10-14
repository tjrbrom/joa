package com.x.hhqs.joa.common.restclient.processor;

import org.apache.http.client.methods.CloseableHttpResponse;

public interface RestClientResultProcessor<T> {
    T parse(CloseableHttpResponse rawResponse) throws Exception;
}
