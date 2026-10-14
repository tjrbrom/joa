package com.x.hhqs.joa.in.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.DocumentException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import com.x.hhqs.joa.in.exception.Rt1QueryException;

@Service
public class RestconfService {
	@Autowired
	private RestClientService restClientService;

	private static final Logger LOGGER = LoggerFactory.getLogger(RestconfService.class);

	public JSONObject getEmaInstance(String modelName, String typeName, String deviceName, String instanceName,
			String requestParams) throws Rt1QueryException, DocumentException, IOException {
		LOGGER.info("getEmaInstance modelName:{}, typeName:{}, deviceName:{}, instanceName:{}, requestParams:{}",
				modelName, typeName, deviceName, instanceName, requestParams);
		JSONObject jsonObject = restClientService.handleGetEmaInstanceRequestAsJson(modelName, typeName, deviceName,
				instanceName, requestParams);
		LOGGER.info(jsonObject.toString(4));
		return jsonObject;
	}

	public String getEmaInstance(String modelName, String typeName, String deviceName, String instanceName,
			String requestParams, String jsonPath)
			throws NoSuchMessageException, Rt1QueryException, DocumentException, IOException {
		JSONObject jsonObject = getEmaInstance(modelName, typeName, deviceName, instanceName, requestParams);
		return getJsonValue(jsonObject, jsonPath);
	}

	public Map<String, String> getEmaInstance(String modelName, String typeName, String deviceName, String instanceName,
			String requestParams, List<String> jsonPathList)
			throws NoSuchMessageException, Rt1QueryException, DocumentException, IOException {
		Map<String, String> emaInstanceMap = new HashMap<String, String>();
		JSONObject jsonObject = getEmaInstance(modelName, typeName, deviceName, instanceName, requestParams);

		for (Iterator<String> iterator = jsonPathList.iterator(); iterator.hasNext();) {
			String jsonPath = (String) iterator.next();
			String jsonValue = getJsonValue(jsonObject, jsonPath);
			emaInstanceMap.put(jsonPath, jsonValue);
		}
		return emaInstanceMap;
	}

	public Set<String> getEmaChildInstance(String modelName, String typeName, String deviceName, String instanceName,
			String childType) throws Rt1QueryException, DocumentException, IOException {
		LOGGER.info("getEmaChildInstance modelName:{}, typeName:{}, deviceName:{}, instanceName:{}, childType:{}",
				modelName, typeName, deviceName, instanceName, childType);
		Set<String> resultSet = restClientService.handleGetEmaChildInstanceOfTypeAsJson(modelName, typeName, deviceName,
				instanceName, childType);
		LOGGER.info(resultSet.toString());
		return resultSet;
	}

	/**
	 * Parse JSONObject by jsonPath
	 * 
	 * @param jsonObject
	 * @param jsonPath   obj1/obj2/...
	 *                   <p>
	 *                   for example: extraAttributes/index
	 * @return
	 * @throws Rt1QueryException
	 */
	private String getJsonValue(JSONObject jsonObject, String jsonPath) throws Rt1QueryException {
		String rstVal = "";
		try {
			String[] pathElements = jsonPath.split("/");
			JSONObject currentObject = jsonObject;
			for (String pathElement : pathElements) {
				Object obj = currentObject.get(pathElement);
				if (obj instanceof JSONObject) {
					currentObject = (JSONObject) obj;
				} else {
					rstVal = String.valueOf(obj);
				}
			}
		} catch (Exception e) {
			// throw new Rt1QueryException(ErrorKey.JSON_PARSE_ERROR, new String[] {
			// jsonPath }, e);
			LOGGER.error("getJsonValue error, jsonPath:" + jsonPath + ", errMsg:" + e.getMessage());
		}
		return rstVal;
	}
}
