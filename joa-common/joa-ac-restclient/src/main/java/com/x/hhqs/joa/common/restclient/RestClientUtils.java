package com.x.hhqs.joa.common.restclient;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class RestClientUtils {
    private static RestClientUtils INSTANCE = new RestClientUtils();
    private static RestClientUtils c_testInstance = null;

    private RestClientUtils() {
    }

    public static RestClientUtils getInstance() {
        return c_testInstance != null ? c_testInstance : INSTANCE;
    }

    public static void setTestInstance(RestClientUtils testInstance) {
        c_testInstance = testInstance;
    }

    /**
     * NOTICE: this method will close the response and subsequent read will fail with
     *  'java.io.IOException: Attempted read from closed stream'
     * @param rawResponse
     * @return
     * @throws IOException
     */
    public String getResponseText(CloseableHttpResponse rawResponse) throws IOException {
        return rawResponse.getEntity() != null ? EntityUtils.toString(rawResponse.getEntity()) : "";
    }
}
