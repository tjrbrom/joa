package com.x.hhqs.joa.mds.emsclient.ac;

import org.json.JSONObject;
import com.x.hhqs.joa.common.restclient.RequestMethod;
import com.x.hhqs.joa.common.restclient.RestconfAuthRestClient;
import com.x.hhqs.joa.common.restclient.annotation.Param;
import com.x.hhqs.joa.common.restclient.annotation.RestClient;
import com.x.hhqs.joa.common.restclient.processor.HttpRequestBaseProcessor;

public interface MdsRestClient extends RestconfAuthRestClient {

    @RestClient(
        method = RequestMethod.POST,
        path = "/rest/netconf/v1/execute?history=flase",
        mediaType = "application/xml",
        byPassCB = true)
    JSONObject processNetconfRequest(HttpRequestBaseProcessor<?> actionProcessor);

    @RestClient(method = RequestMethod.GET, path = "/rest/mds/getAllManagers")
    String getAllManagers();

    @RestClient(method = RequestMethod.GET, path = "/rest/mds/getDevicesWithManagerName/{managerName}")
    String getDevicesWithManagerName(@Param("managerName") String managerName);

}
