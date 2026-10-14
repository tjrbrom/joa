package com.x.hhqs.joa.common.restclient;

import com.x.hhqs.joa.common.restclient.annotation.*;
import com.x.hhqs.joa.common.restclient.annotation.RestClient;
import com.x.hhqs.joa.common.restclient.processor.HttpErrorCodeProcessor;
import com.x.hhqs.joa.common.restclient.processor.HttpRequestBaseProcessor;
import com.x.hhqs.joa.common.restclient.processor.RestClientResultProcessor;
import org.json.JSONObject;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

public interface NavAuthRestClient extends AuthRestClient {
    @RestClient(method = RequestMethod.POST, path = "/rest/netconf/v1/execute?history={history}", mediaType = MediaType.APPLICATION_XML, byPassCB = true)
    JSONObject processNetconfRequest(@Param("history") boolean history, HttpRequestBaseProcessor actionProcessor,
                                     RestClientResultProcessor<JSONObject> parser, HttpErrorCodeProcessor errorCodeProcessor);

    @RestClient(method = RequestMethod.POST, path = "/rest/netconf/v1/execute?history={history}&core-target={target}", mediaType = MediaType.APPLICATION_XML, byPassCB = true)
    JSONObject processNetconfRequest(@Param("history") boolean history, @Param("target") String target, HttpRequestBaseProcessor actionProcessor,
                                     RestClientResultProcessor<JSONObject> parser, HttpErrorCodeProcessor errorCodeProcessor);

    /**
     * Overriding the method declaration in {@link AuthRestClient} in order to
     * bypassCB.
     *
     */
    @Override
    @RestClient(method = RequestMethod.POST, path = "/rest/auth/login", mediaType = "", byPassCB = true)
    @Login
    TokenResponse login();

    @Override
    @RestClient(method = RequestMethod.POST, path = "/rest/auth/logout", mediaType = "")
    @Logout
    void logout();

    @Override
    @RestClient(method = RequestMethod.POST, path = "/rest/auth/refreshAccessToken", mediaType = "", byPassCB = true)
    @RefreshAccessToken
    TokenResponse refreshToken(@FormParam("refresh_token") String refreshToken);

}
