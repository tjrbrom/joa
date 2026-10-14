package com.x.hhqs.joa.rt1agent.server.handler;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class RT1StringEncoder extends MessageToMessageEncoder<CharSequence> {
	private final Charset charset;

	@Autowired
	public RT1StringEncoder(String charsetName) {
		if (Charset.isSupported(charsetName)) {
			this.charset = Charset.forName(charsetName);
		} else {
			this.charset = Charset.forName("UTF-8");
		}
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
		if (msg.length() == 0) {
			return;
		}

		out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), charset));
	}
}
