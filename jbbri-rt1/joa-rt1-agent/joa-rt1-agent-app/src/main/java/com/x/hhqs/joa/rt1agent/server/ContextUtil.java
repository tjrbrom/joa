package com.x.hhqs.joa.rt1agent.server;

import java.util.UUID;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class ContextUtil {

	public static final AttributeKey<SessionInfo> SESSION_KEY = AttributeKey.valueOf("SESSION-"
			+ UUID.randomUUID().toString());

	private ContextUtil() {
	}

	public static void attachSession(ChannelHandlerContext ctx, SessionInfo session) {
		ctx.channel().attr(SESSION_KEY).set(session);
	}

	public static void detachSession(ChannelHandlerContext ctx) {
		ctx.channel().attr(SESSION_KEY).set(null);
	}

	public static SessionInfo getSession(ChannelHandlerContext ctx) {
		return ctx.channel().attr(SESSION_KEY).get();
	}

}
