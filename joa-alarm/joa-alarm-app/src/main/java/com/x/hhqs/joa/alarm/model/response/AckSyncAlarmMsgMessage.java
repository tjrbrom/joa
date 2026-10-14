package com.x.hhqs.joa.alarm.model.response;


import com.x.hhqs.joa.alarm.model.MessageType;

public class AckSyncAlarmMsgMessage extends OmcAckMessage {

	private static final String contentFormat = "ackSyncAlarmMsg;reqId=%s;result=%s;resDesc=%s";

	public AckSyncAlarmMsgMessage() {
		super(MessageType.ackSyncAlarmMsg);
	}

	@Override
	public String getContent() {
		String reqId = getValue("reqId");
		String result = getValue("result");
		String resDesc = getValue("resDesc");
		return String.format(contentFormat, reqId, result, resDesc);
	}

}
