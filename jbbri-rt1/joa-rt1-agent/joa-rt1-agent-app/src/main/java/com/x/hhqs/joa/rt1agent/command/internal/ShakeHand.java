package com.x.hhqs.joa.rt1agent.command.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.x.hhqs.joa.rt1agent.command.InternalCommand;
import com.x.hhqs.joa.rt1agent.command.RT1AgentResponseBuilder;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import io.netty.channel.ChannelHandlerContext;

@Component
public class ShakeHand extends InternalCommand {

	@Autowired
	private RT1AgentResponseBuilder responseBuilder;

	@Override
	public String getCommandCode() {
		return "SHAKEHAND";
	}

	// SHAKEHAND:::CTAG::;
	@Override
	public Object execute(ChannelHandlerContext ctx, RT1InputMessage msg) {
		return responseBuilder.success(msg);
	}

}
