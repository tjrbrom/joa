package com.x.hhqs.joa.alarm.model.response;


import com.x.hhqs.joa.alarm.model.MessageType;

public class AckLoginAlarmMessage extends OmcAckMessage {
	private static final String contentFormat = "ackLoginAlarm;result=%s;resDesc=%s";

	public AckLoginAlarmMessage() {
		super(MessageType.ackLoginAlarm);
	}

	@Override
	public String getContent() {
		String result = getValue("result");
		String resDesc = getValue("resDesc");
		return String.format(contentFormat, result, resDesc);
	}

}
