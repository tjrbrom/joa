package com.x.hhqs.joa.common.restclient;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import javax.ws.rs.HttpMethod;
import java.net.URI;

public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

    @Override
    public String getMethod() {
        return HttpMethod.DELETE;
    }

    public HttpDeleteWithBody(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    public HttpDeleteWithBody(final URI uri) {
        super();
        setURI(uri);
    }

    public HttpDeleteWithBody() {
        super();
    }
}
