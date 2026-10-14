package com.x.hhqs.joa.rt1agent.server.handler;

import java.nio.charset.Charset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class RT1StringDecoder extends MessageToMessageDecoder<ByteBuf> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RT1StringDecoder.class);

	private final Charset charset;

	@Autowired
	public RT1StringDecoder(String charsetName) {
		LOGGER.info("JVM Default Charset: {}", Charset.defaultCharset());
		if (Charset.isSupported(charsetName)) {
			LOGGER.info("Set Charset: {}.", charsetName);
			this.charset = Charset.forName(charsetName);
		} else {
			LOGGER.warn("Use UTF-8 Chaset, config encoding: {} is not support.", charsetName);
			this.charset = Charset.forName("UTF-8");
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		out.add(msg.toString(charset));
	}
}
