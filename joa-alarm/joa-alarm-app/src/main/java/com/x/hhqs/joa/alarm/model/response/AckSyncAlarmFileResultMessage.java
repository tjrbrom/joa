package com.x.hhqs.joa.alarm.model.response;


import com.x.hhqs.joa.alarm.model.MessageType;

public class AckSyncAlarmFileResultMessage extends OmcAckMessage {
	private static final String contentFormat = "ackSyncAlarmFileResult;reqId=%s;result=%s;fileName=%s;resDesc=%s";

	public AckSyncAlarmFileResultMessage() {
		super(MessageType.ackSyncAlarmFileResult);
	}

	@Override
	public String getContent() {
		String reqId = getValue("reqId");
		String result = getValue("result");
		String filename = getValue("filename");
		String resDesc = getValue("resDesc");
		return String.format(contentFormat, reqId, result, filename, resDesc);
	}

}
