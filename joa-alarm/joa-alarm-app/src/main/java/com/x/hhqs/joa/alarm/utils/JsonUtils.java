package com.x.hhqs.joa.alarm.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtils {
	private static final JsonUtils INSTANCE = new JsonUtils();

	private static JsonUtils clientUtils;

	public static void setInstanceForTest(JsonUtils instance) {
		clientUtils = instance;
	}

	private JsonUtils() {}

	public static JsonUtils getInstance() {
		if (clientUtils != null) {
			return clientUtils;
		}
		return INSTANCE;
	}
	
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public String toJson(Object obj) throws JsonProcessingException {
		return MAPPER.writeValueAsString(obj);
	}

	public <T> T fromJson(byte[] json, Class<T> clazz) throws IOException {
		return MAPPER.readValue(json, clazz);
	}
}