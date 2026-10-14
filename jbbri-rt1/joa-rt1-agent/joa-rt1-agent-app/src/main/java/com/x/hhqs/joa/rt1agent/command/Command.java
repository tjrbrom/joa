package com.x.hhqs.joa.rt1agent.command;

import com.x.hhqs.joa.rt1message.RT1InputMessage;

import io.netty.channel.ChannelHandlerContext;

public interface Command {
	
	public String getCommandGroup();

	public String getCommandCode();

	public Object execute(ChannelHandlerContext ctx, RT1InputMessage msg) throws Exception;

}
