package com.x.hhqs.joa.rt1agent.server.handler;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rt1agent.command.EaddCode;
import com.x.hhqs.joa.rt1agent.command.RT1AgentResponseBuilder;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

@Component
@Sharable
public class RT1MessageDecoder extends MessageToMessageDecoder<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RT1MessageDecoder.class);

	@Autowired
	private RT1AgentResponseBuilder responseBuilder;

	/**
	 * <Command Code>:[TID]:[AID]:[CTAG]:[GENERAL][(:<parameter block>)*];
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
		LOGGER.info("Au Receive RT1 Input Message: " + msg.trim());
		
		if (!StringUtils.hasText(msg)) {
			return;
		}
		
		try {
		    msg = StringUtils.trimLeadingWhitespace(msg);
			RT1InputMessage inputMessage = RT1InputMessage.parseInputMessage(msg);
			out.add(inputMessage);
		} catch (Exception e) {
			LOGGER.error("Au parseInputMsg error rt1Msg: " + msg, e);
			ctx.writeAndFlush(responseBuilder.response(EaddCode.PARSE_INPUT_FAIL).toString());
		}
	}

}