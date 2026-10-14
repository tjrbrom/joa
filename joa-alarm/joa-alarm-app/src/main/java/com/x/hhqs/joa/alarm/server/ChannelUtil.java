package com.x.hhqs.joa.alarm.server;

import com.x.hhqs.joa.alarm.model.ClientSession;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.UUID;

public class ChannelUtil {

	public static final AttributeKey<ClientSession> SESSION_KEY = AttributeKey
			.valueOf("session-" + UUID.randomUUID().toString());

	private ChannelUtil() {
	}

	public static void attachSession(Channel ch, ClientSession session) {
		ch.attr(SESSION_KEY).set(session);
	}

	public static ClientSession detachSession(Channel ch) {
		return ch.attr(SESSION_KEY).getAndRemove();
	}

	public static ClientSession getSession(Channel ch) {
		return ch.attr(SESSION_KEY).get();
	}

}