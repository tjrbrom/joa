package com.x.hhqs.joa.alarm.model.response;


import com.x.hhqs.joa.alarm.model.MessageType;

public class AckHeartBeatMessage extends OmcAckMessage {
	private static final String contentFormat = "ackHeartBeat;reqId=%s";

	public AckHeartBeatMessage() {
		super(MessageType.ackHeartBeat);
	}

	@Override
	public String getContent() {
		String reqId = getValue("reqId");
		return String.format(contentFormat, reqId);
	}

}
