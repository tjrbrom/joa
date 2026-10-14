package com.x.hhqs.joa.alarm.model;

public enum MessageType {

	realTimeAlarm(0, "realTimeAlarm"),
	reqLoginAlarm(1, "reqLoginAlarm", "ackLoginAlarm"),
	ackLoginAlarm(2, "ackLoginAlarm"),
	reqSyncAlarmMsg(3, "reqSyncAlarmMsg", "ackSyncAlarmMsg"),
	ackSyncAlarmMsg(4, "ackSyncAlarmMsg"),
	reqSyncAlarmFile(5, "reqSyncAlarmFile", "ackSyncAlarmFile"),
	ackSyncAlarmFile(6, "ackSyncAlarmFile"),
	ackSyncAlarmFileResult(7, "ackSyncAlarmFileResult"),
	reqHeartBeat(8, "reqHeartBeat", "ackHeartBeat"),
	ackHeartBeat(9, "ackHeartBeat"),
	closeConnAlarm(10, "closeConnAlarm"),
	reqSyncElementFile(101, "reqSyncElementFile", "ackSyncElementFile"),
	ackSyncElementFile(102, "ackSyncElementFile"),
	ackSyncElementFileResult(103, "ackSyncElementFileResult"),
	undefined(-1, "undefined");

	private int value = -1;
	private String messageName = null;
	private String responseName = null;

	private MessageType(int value, String messageName) {
		this.value = value;
		this.messageName = messageName;
		this.responseName = messageName;
	}

	private MessageType(int value, String messageName, String responseName) {
		this.value = value;
		this.messageName = messageName;
		this.responseName = responseName;
	}

	public static MessageType getType(int value) {
		for (MessageType type : MessageType.values()) {
			if (type.value == value) {
				return type;
			}
		}
		return undefined;
	}

	public static MessageType getType(String messageName) {
		if (messageName == null || messageName.isEmpty())
			return undefined;

		messageName = messageName.toLowerCase();
		for (MessageType type : MessageType.values()) {
			if (type.messageName.toLowerCase().equals(messageName)) {
				return type;
			}
		}
		return undefined;
	}

	public int getValue() {
		return value;
	}

	public String getMessageName() {
		return messageName;
	}

	public MessageType getResponseType() {
		return getType(responseName);
	}

}
