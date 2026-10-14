package com.x.hhqs.joa.alarm.handler.request;

import com.google.common.base.Strings;
import com.x.hhqs.joa.alarm.handler.RequestMessageHandler;
import com.x.hhqs.joa.alarm.model.MessageType;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import com.x.hhqs.joa.alarm.model.response.OmcAckMessage;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * request: reqHeartBeat;reqId=33<br>
 * response: ackHeartBeat;reqId=33<br>
 */
@Component
@Slf4j
public class ReqHeartBeatHandler extends RequestMessageHandler {

	@Override
	public void executeRequest(Channel ch, OmcRequest request) {
		log.info("Receive heartbeat: " + request.getContent());
		success(ch, request);
	}

	private void success(Channel ch, OmcRequest request) {
		String reqId = request.getValue("reqId");
		if (Strings.isNullOrEmpty(reqId)) {
			reqId = "-1";
		}

		OmcAckMessage ackMessage = getOmcAckMessage();
		ackMessage.setValue("reqId", reqId);
		ch.writeAndFlush(ackMessage);
		log.info("Send heartbeat: " + ackMessage.getContent());
	}

	@Override
	public MessageType getType() {
		return MessageType.reqHeartBeat;
	}

}
