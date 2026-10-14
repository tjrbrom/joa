package com.x.hhqs.joa.common.restclient;

import org.apache.http.client.methods.*;

import java.lang.reflect.InvocationTargetException;


public enum RequestMethod {
    GET(HttpGet.class), POST(HttpPost.class), DELETE(HttpDelete.class), DELETE_WITH_BODY(HttpDeleteWithBody.class), PUT(HttpPut.class), OPTION(HttpOptions.class),
    PATCH(HttpPatch.class), TRACE(HttpTrace.class);

    private final Class<?> m_clazz;

    RequestMethod(Class<?> clazz) {
        m_clazz = clazz;
    }

    public HttpRequestBase createHttpMethodInstance(String url) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (HttpRequestBase) m_clazz.getConstructor(String.class).newInstance(url);
    }
}
