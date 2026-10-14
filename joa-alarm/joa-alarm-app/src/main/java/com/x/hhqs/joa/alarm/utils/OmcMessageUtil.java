package com.x.hhqs.joa.alarm.utils;


import com.x.hhqs.joa.alarm.model.MessageType;
import com.x.hhqs.joa.alarm.model.OmcRequest;

public class OmcMessageUtil {

	public static OmcRequest parseRequestMessage(String input) {
		String[] result = input.split("[;]");
		String name = result[0].trim();

		if (name.isEmpty()) {
			return null;
		}

		MessageType messageType = MessageType.getType(name);
		OmcRequest request = new OmcRequest(messageType, input);

		for (int i = 1; i < result.length; i++) {
			String[] values = result[i].split("[=]", 2);

			String key = values[0].trim();
			if (key.isEmpty() || values.length < 2) {
				continue;
			}

			String value = values[1].trim();
			// In OMC, value equals to String "null" means the parameter is
			// empty or not exist, or we should not put it to map
			if (value.equalsIgnoreCase("null")) {
				continue;
			}

			request.putParameter(key, value);
		}

		return request;
	}

}
