package com.x.hhqs.joa.rt1agent.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

@Service
public class SessionConnectionManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionConnectionManager.class);

	// Key: sessionId
	private Map<String, ChannelHandlerContext> clients = new ConcurrentHashMap<>();

	public void addClient(final String sessionId, ChannelHandlerContext ctx) {
		clients.put(sessionId, ctx);
	}

	public void removeClient(final String sessionId) {
		clients.remove(sessionId);
	}

	public ChannelHandlerContext getClient(String sessionId) {
		return clients.get(sessionId);
	}

	public void send(final String sessionId, final String response) {
		ChannelHandlerContext ctx = clients.get(sessionId);
		LOGGER.info("Au Send RT1 Response Message: \r\n" + response);

		if (ctx == null) {
			LOGGER.warn("Cannot find user session, id = " + sessionId);
			return;
		}

		ctx.channel().writeAndFlush(response).addListener(new ResultListener(ctx, response));
	}

	class ResultListener implements ChannelFutureListener {
		private ChannelHandlerContext ctx;
		private String response;

		public ResultListener(ChannelHandlerContext ctx, String response) {
			this.ctx = ctx;
			this.response = response;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (!future.isSuccess()) {
				LOGGER.error(String.format("Send response to %s fail:%n %s", ctx.channel().remoteAddress().toString(),
						response));
			}
		}
	}

}
