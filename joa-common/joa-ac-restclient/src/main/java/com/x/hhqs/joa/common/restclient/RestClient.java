package com.x.hhqs.joa.common.restclient;

import com.x.hhqs.joa.common.restclient.annotation.Param;
import com.x.hhqs.joa.common.restclient.annotation.RawBodyData;
import com.x.hhqs.joa.common.restclient.processor.HttpErrorCodeProcessor;
import com.x.hhqs.joa.common.restclient.processor.HttpRequestBaseProcessor;
import com.x.hhqs.joa.common.restclient.processor.RestClientResultProcessor;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.util.Set;

/**
 * Used to extend interface methods
 */
public interface RestClient extends RestconfAuthRestClient {

    /**
     * Call this method if you don't need to use the result processors.
     *
     * @param history
     * @param actionProcessor
     * @return
     */
    @com.x.hhqs.joa.common.restclient.annotation.RestClient(method = RequestMethod.POST, path = "/rest/netconf/v1/execute?history={history}", mediaType = MediaType.APPLICATION_XML, byPassCB = true)
    JSONObject processNetconfRequest(@Param("history") boolean history, HttpRequestBaseProcessor actionProcessor);

    @com.x.hhqs.joa.common.restclient.annotation.RestClient(method = RequestMethod.GET, path = "/rest/ema/entity/{modelName}/{typeName}/{deviceName}/{instanceName}?{requestParams}", mediaType = ProxyConstants.TYPE_YANG_DATA_JSON, acceptMediaType = ProxyConstants.TYPE_DATA_JSON)
    JSONObject handleGetEmaInstanceRequestAsJson(@Param("modelName") String modelName,
                                                 @Param("typeName") String typeName, @Param("deviceName") String deviceName,
                                                 @Param("instanceName") String instanceName, @Param("requestParams") String requestParams,
                                                 RestClientResultProcessor<JSONObject> processor, HttpErrorCodeProcessor errorProcessor);

    @com.x.hhqs.joa.common.restclient.annotation.RestClient(
            method = RequestMethod.POST,
            path = "/rest/ema/app/network-views/entity/childs/{modelName}/{typeName}/{deviceName}?{requestParams}",
            mediaType = ProxyConstants.TYPE_DATA_JSON, acceptMediaType = ProxyConstants.TYPE_DATA_JSON
    )
    JSONArray handleGetEmaBulkInstanceOfTypeAsJson(@Param("modelName") String modelName, @Param("typeName") String typeName, @Param("deviceName") String deviceName,
                                                   @Param("requestParams") String requestParams, @RawBodyData String input, RestClientResultProcessor<JSONArray> processor, HttpErrorCodeProcessor errorProcessor);


    @com.x.hhqs.joa.common.restclient.annotation.RestClient(method = RequestMethod.GET, path = "/rest/ema/entity/{modelName}/{typeName}/{deviceName}/{instanceName}/{childType}", mediaType = ProxyConstants.TYPE_YANG_DATA_JSON, acceptMediaType = ProxyConstants.TYPE_DATA_JSON)
    Set<String> handleGetEmaChildInstanceOfTypeAsJson(@Param("modelName") String modelName,
                                                      @Param("typeName") String typeName, @Param("deviceName") String deviceName,
                                                      @Param("instanceName") String instanceName, @Param("childType") String childType,
                                                      RestClientResultProcessor<Set<String>> processor, HttpErrorCodeProcessor errorProcessor);

}
