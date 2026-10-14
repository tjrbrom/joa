package com.x.hhqs.joa.alarm.model;

import com.x.hhqs.joa.alarm.api.Command;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class OmcRequest implements OmcMessage, Command {

	private MessageType messageType;

	private Map<String, String> params = new LinkedHashMap<>();

	private String content;

	public OmcRequest(MessageType messageType, String content) {
		this.messageType = messageType;
		this.content = content;
	}

	@Override
	public MessageType getMessageType() {
		return messageType;
	}

	@Override
	public String getContent() {
		return content;
	}

	public void putParameter(String name, String value) {
		params.put(name.toLowerCase(), value);
	}

	public String getValue(String name) {
		String value = params.get(name.toLowerCase());

		if (value == null) {
			log.warn("Value of [" + name + "] is null.");
		}

		return value;
	}

	public Long getLongValue(String name) {
		String v = getValue(name);
		return Long.parseLong(v);
	}

	public boolean containsParameter(String name) {
		return params.containsKey(name.toLowerCase());
	}

	@Override
	public String toString() {
		return "OmcRequest [messageType=" + messageType + ", content=" + content + "]";
	}

}
