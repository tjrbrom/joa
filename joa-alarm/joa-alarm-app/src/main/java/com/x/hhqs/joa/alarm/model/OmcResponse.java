package com.x.hhqs.joa.alarm.model;


import com.x.hhqs.joa.alarm.api.Response;

public class OmcResponse implements OmcMessage, Response {

	private MessageType messageType;

	private String content;

	public OmcResponse(MessageType messageType) {
		super();
		this.messageType = messageType;
	}

	public OmcResponse(MessageType messageType, String content) {
		super();
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

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "OmcResponse [messageType=" + messageType + ", content=" + content + "]";
	}
}
