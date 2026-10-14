package com.x.hhqs.joa.alarm.model.response;


import com.x.hhqs.joa.alarm.model.MessageType;

public class DefaultAckMessage extends OmcAckMessage {
	private static final String contentFormat = ";reqId=%s;result=%s;resDesc=%s";

	public DefaultAckMessage(MessageType responseType) {
		super(responseType);
	}

	@Override
	public String getContent() {
		String reqId = getValue("reqId");
		String result = getValue("result");
		String resDesc = getValue("resDesc");
		return getMessageType().getMessageName() + String.format(contentFormat, reqId, result, resDesc);
	}

}
