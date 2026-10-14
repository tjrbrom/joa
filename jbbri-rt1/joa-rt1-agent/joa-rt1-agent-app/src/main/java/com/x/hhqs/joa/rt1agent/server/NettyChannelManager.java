package com.x.hhqs.joa.rt1agent.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

@Component
public class NettyChannelManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyChannelManager.class);

	private final Map<ChannelId, Channel> channels = new ConcurrentHashMap<>();

	public void add(Channel channel) {
		channels.put(channel.id(), channel);
		LOGGER.info("[add][Connection ({}) joined]", channel.id());
	}

	public void remove(Channel channel) {
		channels.remove(channel.id());
		LOGGER.info("[remove][Connection ({}) left]", channel.id());
	}

}
