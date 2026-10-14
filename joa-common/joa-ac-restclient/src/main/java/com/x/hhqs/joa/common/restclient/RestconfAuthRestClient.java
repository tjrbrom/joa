package com.x.hhqs.joa.common.restclient;

import com.x.hhqs.joa.common.restclient.processor.HttpErrorCodeProcessor;
import com.x.hhqs.joa.common.restclient.processor.RestClientResultProcessor;
import com.x.hhqs.joa.common.restclient.annotation.Param;
import com.x.hhqs.joa.common.restclient.annotation.RawBodyData;
import com.x.hhqs.joa.common.restclient.annotation.RestClient;

import javax.ws.rs.core.Response;

public interface RestconfAuthRestClient extends NavAuthRestClient {
    @RestClient(method = RequestMethod.GET, path = "/rest/restconf/data/{path}",
            mediaType = ProxyConstants.TYPE_YANG_DATA_JSON, acceptMediaType = ProxyConstants.TYPE_YANG_DATA_JSON)
    Response handleGetAsJson(@Param("path") String path, RestClientResultProcessor<Response> processor, HttpErrorCodeProcessor errorProcessor);

    @RestClient(method = RequestMethod.GET, path = "/rest/restconf/data/{path}",
            mediaType = ProxyConstants.TYPE_YANG_DATA_XML, acceptMediaType = ProxyConstants.TYPE_YANG_DATA_XML)
    Response handleGetAsXml(@Param("path") String path, RestClientResultProcessor<Response> processor, HttpErrorCodeProcessor errorProcessor);

    @RestClient(method = RequestMethod.PATCH, path = "/rest/restconf/data/{path}",
            mediaType = ProxyConstants.TYPE_YANG_DATA_JSON, acceptMediaType = ProxyConstants.TYPE_YANG_DATA_JSON)
    Response handlePatchAsJson(@Param("path") String path, @RawBodyData String input, RestClientResultProcessor<Response> processor, HttpErrorCodeProcessor errorProcessor);

    @RestClient(method = RequestMethod.POST, path = "/rest/restconf/operations/{path}",
            mediaType = ProxyConstants.TYPE_YANG_DATA_JSON, acceptMediaType = ProxyConstants.TYPE_YANG_DATA_JSON)
    Response handleOperationAsJson(@Param("path") String path, @RawBodyData String input, RestClientResultProcessor<Response> processor, HttpErrorCodeProcessor errorProcessor);

    @RestClient(method = RequestMethod.POST, path = "/rest/restconf/operations/{path}",
            mediaType = ProxyConstants.TYPE_YANG_DATA_XML, acceptMediaType = ProxyConstants.TYPE_YANG_DATA_XML)
    Response handleOperationAsXml(@Param("path") String path, @RawBodyData String input, RestClientResultProcessor<Response> processor, HttpErrorCodeProcessor errorProcessor);

    @RestClient(method = RequestMethod.POST, path = "/rest/restconf/data/{path}",
            mediaType = ProxyConstants.TYPE_YANG_DATA_JSON, acceptMediaType = ProxyConstants.TYPE_YANG_DATA_JSON)
    Response handlePostAsJson(@Param("path") String path, @RawBodyData String input, RestClientResultProcessor<Response> processor, HttpErrorCodeProcessor errorProcessor);

    @RestClient(method = RequestMethod.DELETE, path = "/rest/restconf/data/{path}",
            mediaType = ProxyConstants.TYPE_YANG_DATA_JSON, acceptMediaType = ProxyConstants.TYPE_YANG_DATA_JSON)
    Response handleDeleteAsJson(@Param("path") String path, RestClientResultProcessor<Response> processor, HttpErrorCodeProcessor errorProcessor);

    @RestClient(method = RequestMethod.DELETE, path = "/rest/restconf/data/{path}",
            mediaType = ProxyConstants.TYPE_YANG_DATA_XML, acceptMediaType = ProxyConstants.TYPE_YANG_DATA_XML)
    Response handleDeleteAsXml(@Param("path") String path, RestClientResultProcessor<Response> processor, HttpErrorCodeProcessor errorProcessor);

    @RestClient(method = RequestMethod.PATCH, path = "/rest/restconf/data/{path}",
            mediaType = ProxyConstants.TYPE_YANG_DATA_XML, acceptMediaType = ProxyConstants.TYPE_YANG_DATA_XML)
    Response handlePatchAsXml(@Param("path") String path, @RawBodyData String input, RestClientResultProcessor<Response> processor, HttpErrorCodeProcessor errorProcessor);

    @RestClient(method = RequestMethod.PUT, path = "/rest/restconf/data/{path}",
            mediaType = ProxyConstants.TYPE_YANG_DATA_XML, acceptMediaType = ProxyConstants.TYPE_YANG_DATA_XML)
    Response handlePutAsXml(@Param("path") String path, @RawBodyData String input, RestClientResultProcessor<Response> processor, HttpErrorCodeProcessor errorProcessor);

}
