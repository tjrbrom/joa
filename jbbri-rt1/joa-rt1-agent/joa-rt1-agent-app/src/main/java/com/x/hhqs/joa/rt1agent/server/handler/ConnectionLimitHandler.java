package com.x.hhqs.joa.rt1agent.server.handler;

import java.util.concurrent.atomic.AtomicInteger;

import com.x.hhqs.joa.rt1agent.config.ConnectionConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Component
@Sharable
public class ConnectionLimitHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionLimitHandler.class);

	private static final AtomicInteger connections = new AtomicInteger(0);

	@Autowired
	private ConnectionConfigurationProperties connectionProperties;

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		if (getMaxConnections() != -1) {
			int currentCount = connections.getAndIncrement();

			if (currentCount + 1 > getMaxConnections()) {
				LOGGER.warn("Maximum client numbers. The conncection is to be rejected!");
				ctx.channel().close();
				// return?
			}
		}

		super.channelActive(ctx);
	}

	private int getMaxConnections() {
		return connectionProperties.getMaxConnections();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (getMaxConnections() != -1) {
			connections.decrementAndGet();
		}
		
		super.channelInactive(ctx);
	}
}
