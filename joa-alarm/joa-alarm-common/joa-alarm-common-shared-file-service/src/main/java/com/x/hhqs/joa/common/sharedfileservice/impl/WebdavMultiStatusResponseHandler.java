package com.x.hhqs.joa.common.sharedfileservice.impl;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.handler.MultiStatusResponseHandler;
import com.github.sardine.model.Multistatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class WebdavMultiStatusResponseHandler extends MultiStatusResponseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebdavMultiStatusResponseHandler.class);
    
    @Override
    public Multistatus handleResponse(HttpResponse response) throws IOException {
        super.validateResponse(response);

        // Process the response from the server.
        HttpEntity entity = response.getEntity();
        StatusLine statusLine = response.getStatusLine();
        if (entity == null) {
            throw new SardineException("No entity found in response", statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }
        try {
            return this.getMultistatus(entity.getContent());
        } catch (IOException e) {
            // JAXB error unmarshalling response stream
            LOGGER.error("Error when handling response", e);
            throw new SardineException(e.getMessage(), statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
    }

    protected Multistatus getMultistatus(InputStream stream) throws IOException {
        return WebdavSardineUtil.unmarshal(stream);
    }
}