package com.x.hhqs.joa.in.service;

import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.ErrorKey;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.util.RpcUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.dom4j.DocumentException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.x.hhqs.joa.common.restclient.RestClient;
import com.x.hhqs.joa.common.restclient.RestClientUtils;
import com.x.hhqs.joa.common.restclient.processor.HttpErrorCodeProcessor;
import com.x.hhqs.joa.common.restclient.processor.HttpRequestBaseProcessor;
import com.x.hhqs.joa.common.restclient.processor.RestClientResultProcessor;

@Service
@RefreshScope
public class RestClientService {
	@Autowired
	private RestClient restClient;

	@Value("${core.virtualizer.target}")
	private String virtualizerTarget;

	private static final Logger LOG = LoggerFactory.getLogger(RestClientService.class);

	public JSONObject processNetconfRequest(String netconfReqXml)
			throws Rt1QueryException, DocumentException, IOException {
		return checkAndRtnJSONObject(restClient.processNetconfRequest(false,
				httpRequestProcessor(netconfReqXml, ContentType.APPLICATION_XML), jsonResultParser(),
				httpErrorCodeProcessor()));
	}

	// /rest/netconf/v1/execute/?core-target=<virtualizerTarget>
	public JSONObject processAvNetconfRequest(String netconfReqXml)
			throws Rt1QueryException, DocumentException, IOException {
		return checkAndRtnJSONObject(restClient.processNetconfRequest(false, virtualizerTarget,
				httpRequestProcessor(netconfReqXml, ContentType.APPLICATION_XML), jsonResultParser(),
				httpErrorCodeProcessor()));
	}

	// /rest/ema/entity/<modelName>/<typeName>/<deviceName>/<instanceName>?fetchDeviceAttributes=<>&isChild=<>&isOne=<>
	// /rest/ema/entity/LS-FX-MF-SF-LT/ChannelPartition/FX-TEST1.LT2/BSO_Fiber_CPART_GPON?fetchDeviceAttributes=true&isChild=false&isOne=true
	public JSONObject handleGetEmaInstanceRequestAsJson(String modelName, String typeName, String deviceName,
			String instanceName, String requestParams) throws Rt1QueryException, DocumentException, IOException {
		return checkAndRtnJSONObject(restClient.handleGetEmaInstanceRequestAsJson(modelName, typeName, deviceName,
				instanceName, requestParams, jsonResultParser(), httpErrorCodeProcessor()));
	}

	// /rest/ema/entity/<modelName>/<typeName>/<deviceName>/<instanceName>/<childType>
	// /rest/ema/entity/LS-FX-MF-SF-LT/UNI/realMF14_193.101.LT2/VENET_NBELFA45915D_VEIP/VLAN%20Interface
	public Set<String> handleGetEmaChildInstanceOfTypeAsJson(String modelName, String typeName, String deviceName,
			String instanceName, String childType) throws Rt1QueryException, DocumentException, IOException {
		return restClient.handleGetEmaChildInstanceOfTypeAsJson(modelName, typeName, deviceName, instanceName,
				childType, setResultParser(), httpErrorCodeProcessor());
	}

	private JSONObject checkAndRtnJSONObject(JSONObject jsonObject)
			throws Rt1QueryException, DocumentException, IOException {
		if (jsonObject.has(RpcConsts.HTTP_ERR_CODE)) {
			String statusCode = jsonObject.getString(RpcConsts.HTTP_ERR_CODE);
			String errMsg = jsonObject.toString();
			if (jsonObject.has(RpcConsts.FORMATTED_DEFAULT_MESSAGE)) {
				errMsg = jsonObject.getString(RpcConsts.FORMATTED_DEFAULT_MESSAGE);
			}
			throw new Rt1QueryException(ErrorKey.RESTCONF_HTTP_RESPONSE_ERROR, new String[] { statusCode, errMsg });
		}

		if (jsonObject.has(RpcConsts.ERROR) && jsonObject.getBoolean(RpcConsts.ERROR)) {
			String response = jsonObject.getString(RpcConsts.RESPONSE);
			LOG.info(RpcUtil.formatXml(response));
			String errMsg = RpcUtil.getFirstValueByRegex(response, "<error-message>(.*?)</error-message>");
			throw new Rt1QueryException(ErrorKey.RESTCONF_RESPONSE_ERROR, new String[] { errMsg });
		}
		return jsonObject;
	}

	private HttpRequestBaseProcessor<HttpPost> httpRequestProcessor(String netconfReqXml, ContentType contentType) {
		return method -> method.setEntity(new StringEntity(netconfReqXml, contentType));
	}

	private RestClientResultProcessor<JSONObject> jsonResultParser() {
		return rawResponse -> new JSONObject(RestClientUtils.getInstance().getResponseText(rawResponse));
	}

	private RestClientResultProcessor<Set<String>> setResultParser() {
		return rawResponse -> {
			String response = RestClientUtils.getInstance().getResponseText(rawResponse);
			// ["VSI_ONT1_VEIP_1","VSI_ONT1_VEIP_2"]
			String[] items = response.substring(1, response.length() - 1).split(",");
			Set<String> resultSet = Arrays.stream(items).map(String::trim).collect(Collectors.toSet());
			return resultSet;
		};
	}

	private HttpErrorCodeProcessor httpErrorCodeProcessor() {
		return (statusCode, responseText) -> {
			JSONObject jsonObject = new JSONObject(responseText);
			jsonObject.put(RpcConsts.HTTP_ERR_CODE, String.valueOf(statusCode));
			return jsonObject;
		};
	}
}
