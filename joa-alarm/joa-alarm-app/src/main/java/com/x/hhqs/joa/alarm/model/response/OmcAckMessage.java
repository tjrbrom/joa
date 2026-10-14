package com.x.hhqs.joa.alarm.model.response;



import com.x.hhqs.joa.alarm.model.MessageType;
import com.x.hhqs.joa.alarm.model.OmcResponse;

import java.util.HashMap;
import java.util.Map;

public class OmcAckMessage extends OmcResponse {

	public static final String SUCCESS = "succ";

	public static final String FAIL = "fail";

	private final Map<String, String> values = new HashMap<>();

	public static OmcAckMessage getAckMessage(MessageType responseType) {
		switch (responseType) {
			case ackLoginAlarm:
				return new AckLoginAlarmMessage();
			case ackSyncAlarmMsg:
				return new AckSyncAlarmMsgMessage();
			case ackSyncAlarmFile:
				return new AckSyncAlarmFileMessage();
			case ackSyncAlarmFileResult:
				return new AckSyncAlarmFileResultMessage();
			case ackHeartBeat:
				return new AckHeartBeatMessage();
			default:
				break;
		}

		return new DefaultAckMessage(responseType);
	}

	public OmcAckMessage(MessageType messageType) {
		super(messageType);
	}

	public void setValue(String key, String value) {
		values.put(key.toLowerCase(), value);
	}

	public String getValue(String key) {
		if (key == null)
			return null;

		return values.get(key.toLowerCase());
	}

}
