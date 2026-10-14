package com.x.hhqs.joa.alarm.service.impl;

import com.x.hhqs.joa.alarm.dto.AlarmTypeInfo;
import com.x.hhqs.joa.alarm.service.ACRestService;
import com.x.hhqs.joa.common.restclient.Pair;
import com.x.hhqs.joa.common.restclient.RestClient;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ACRestServiceImpl implements ACRestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ACRestServiceImpl.class);

    private final RestClient restClient;

    public ACRestServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<Pair<String, String>> getAllManagerNameTypePairs() {
        List<Pair<String, String>> managerPairList = new ArrayList<>();
        Response response = restClient.handleGetAsJson("manager-directory-service:manager-directory",
                rawResponse -> {
                    return Response.ok(getResponseText(rawResponse)).build();
                }, (statusCode, responseText) -> {
                    LOGGER.debug("Send handleGetAsJson REST failure with status code:{}, response: {}", statusCode, responseText);
                    return Response.status(statusCode).entity(responseText).build();
                });
        if (response.getStatus() == 200) {
            JSONObject jsonObject = new JSONObject(response.getEntity().toString());
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("manager-directory-service:manager-directory");
            JSONArray jsonArray = jsonObject1.getJSONArray("manager-info");
            int number = jsonArray.length();
            for (int i = 0; i < number; i++) {
                String name = jsonArray.getJSONObject(i).getString("name");
                String managerType = jsonArray.getJSONObject(i).getString("manager-type");
                managerPairList.add(new Pair<String, String>(name, managerType));
            }
        }
        return managerPairList;
    }

    @Override
    public List<AlarmTypeInfo> getAllSupportedAlarmTypesByCoreTarget(String target) {
        ArrayList<AlarmTypeInfo> supportedAlarmTypes = new ArrayList<>();
        Response response = restClient.handleGetAsJson("anv-alarms:alarms?core-target=" + target + "&content=all&depth=3",
                rawResponse -> {
                    return Response.ok(getResponseText(rawResponse)).build();
                }, (statusCode, responseText) -> {
                    LOGGER.debug("Send handleGetAsJson REST failure with status code:{}, response: {}", statusCode, responseText);
                    return Response.status(statusCode).entity(responseText).build();
                });
        if (response.getStatus() == 200) {
            JSONObject jsonObject = new JSONObject(response.getEntity().toString());
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("anv-alarms:alarms");
            JSONArray jsonArray = jsonObject1.getJSONArray("supported-alarm-types");
            int number = jsonArray.length();
            for (int i = 0; i < number; i++) {
                JSONObject alarmObj = jsonArray.getJSONObject(i);
                String alarmTypeId = alarmObj.getString("alarm-type-id");
                String alarmTypeQualifier = alarmObj.getString("alarm-type-qualifier");
                JSONArray reporters = alarmObj.getJSONArray("alarm-type-reporter");
                Set<String> alarmTypeReporter = new HashSet<>();
                if (reporters != null) {
                    int num = reporters.length();
                    for (int j = 0; j < num; j++) {
                        alarmTypeReporter.add(reporters.getString(j));
                    }
                }
                String friendlyName = alarmObj.getString("friendly-name");
                AlarmTypeInfo alarmType = new AlarmTypeInfo(alarmTypeId, alarmTypeQualifier, friendlyName);
                alarmType.setAlarmTypeReporter(alarmTypeReporter);
                supportedAlarmTypes.add(alarmType);
            }
        }
        return supportedAlarmTypes;
    }

    @Override
//    @Cacheable(cacheNames = "retrieveCoreSiteName", key = "'singleton'")
    public String retrieveCoreSiteName() {
        String siteName = "";
        Response response = restClient.handleGetAsJson("site:site",
                rawResponse -> {
                    return Response.ok(getResponseText(rawResponse)).build();
                }, (statusCode, responseText) -> {
                    LOGGER.debug("Send handleGetAsJson REST failure with status code:{}, response: {}", statusCode, responseText);
                    return Response.status(statusCode).entity(responseText).build();
                });
        if (response.getStatus() == 200) {
            JSONObject jsonObject = new JSONObject(response.getEntity().toString());
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("site:site");
            siteName = jsonObject1.getString("current-site-name");
        }
        return siteName;
    }

    private String getResponseText(CloseableHttpResponse rawResponse) throws Exception {
        return rawResponse.getEntity() != null ? EntityUtils.toString(rawResponse.getEntity(), StandardCharsets.UTF_8) : "";
    }
}
