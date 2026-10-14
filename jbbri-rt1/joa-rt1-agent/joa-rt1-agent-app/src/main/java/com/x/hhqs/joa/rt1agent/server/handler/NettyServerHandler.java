package com.x.hhqs.joa.rt1agent.server.handler;

import com.x.hhqs.joa.rt1agent.server.NettyChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);

	@Autowired
	private NettyChannelManager channelManager;

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		channelManager.add(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		channelManager.remove(ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOGGER.error("[exceptionCaught][connection({}) exception occurs]", ctx.channel().id(), cause);
		ctx.channel().close();
	}

}
