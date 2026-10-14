package com.x.hhqs.joa.alarm.model.response;


import com.x.hhqs.joa.alarm.model.MessageType;

public class AckSyncAlarmFileMessage extends OmcAckMessage {
	private static final String contentFormat = "ackSyncAlarmFile;reqId=%s;result=%s;resDesc=%s";

	public AckSyncAlarmFileMessage() {
		super(MessageType.ackSyncAlarmFile);
	}

	@Override
	public String getContent() {
		String reqId = getValue("reqId");
		String result = getValue("result");
		String resDesc = getValue("resDesc");
		return String.format(contentFormat, reqId, result, resDesc);
	}

}
