package com.x.hhqs.joa.alarm.server.handler;


import com.x.hhqs.joa.alarm.model.OmcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.List;

@Slf4j
public class OmcMessageEncoder extends MessageToMessageEncoder<OmcMessage> {


	private final Charset charset;

	public OmcMessageEncoder() {
		// this(Charset.defaultCharset());
		this(Charset.forName("UTF-8"));
	}

	public OmcMessageEncoder(String charsetName) {
		if (Charset.isSupported(charsetName)) {
			this.charset = Charset.forName(charsetName);
		} else {
			this.charset = Charset.forName("UTF-8");
		}
	}

	public OmcMessageEncoder(Charset charset) {
		if (charset == null) {
			throw new NullPointerException("charset");
		}
		this.charset = charset;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, OmcMessage msg, List<Object> out) throws Exception {
		byte[] body = msg.getContent().getBytes(charset);

		ByteBuf buf = ctx.alloc().buffer();
		buf.writeShort(0xFFFF);
		buf.writeByte(msg.getMessageType().getValue());
		buf.writeInt((int) (System.currentTimeMillis() / 1000));
		buf.writeShort(body.length);
		buf.writeBytes(body);

		out.add(buf);

		log.info("Send response: " + getHexBinaryHeader(buf) + " " + msg.getContent());
	}

	private String getHexBinaryHeader(ByteBuf buf) {
		byte[] bytes = new byte[9];
		int readerIndex = buf.readerIndex();
		buf.getBytes(readerIndex, bytes);
		return DatatypeConverter.printHexBinary(bytes);
	}

	public Charset getCharset() {
		return charset;
	}
}
