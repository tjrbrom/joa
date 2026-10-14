package com.x.hhqs.joa.alarm.server.handler;


import com.x.hhqs.joa.alarm.model.MessageType;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import com.x.hhqs.joa.alarm.model.OmcResponse;
import com.x.hhqs.joa.alarm.utils.OmcMessageUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Sharable
@Slf4j
public class OmcRequestDecoder extends MessageToMessageDecoder<String> {

	private static final String INVALID_COMMAND = "undefined;result=fail;resDesc=Invalid Command";
	private static final OmcResponse response = new OmcResponse(MessageType.undefined, INVALID_COMMAND);

	@Override
	protected void decode(ChannelHandlerContext ctx, String content, List<Object> out) throws Exception {
		if (content.isEmpty())
			return;

		OmcRequest request = OmcMessageUtil.parseRequestMessage(content);
		if (request == null || request.getMessageType() == MessageType.undefined) {
			log.error("Invalid Command: " + content);
			ctx.writeAndFlush(response);
			return;
		}
		
		if (request.getMessageType().equals(MessageType.reqLoginAlarm)) {
			log.info("Received request: reqLoginAlarm;user=" + request.getValue("user") + ";key=******;type="
					+ request.getValue("type"));
		} else {
			log.info("Received request: " + content);
		}

		out.add(request);
	}

}