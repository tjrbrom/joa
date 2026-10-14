package com.x.hhqs.joa.alarm.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.List;

/**
 * <pre>
 * Header (9 bytes) | content (lenOfBody bytes)
 * +-----------+---------+-----------+-----------+------------------+      +------------------+
 * | startSign | msgType | timeStamp | lenOfBody | Actual Content   |----->| Actual Content   |
 * | 0xFFFF    | 1 byte  | 4 bytes   | 2 bytes   | reqLoginAlarm;.. |      | reqLoginAlarm;.. |   
 * +-----------+---------+-----------+-----------+------------------+      +------------------+
 * </pre>
 */

@Slf4j
public class OmcMessageDecoder extends ByteToMessageDecoder {

	private enum State {
		HEADER_START, //
		HEADER_END, //
		MSG_TYPE_READ, //
		TIMESTAMP_READ, //
		LENGTH_OF_BODY_READ,//
	}

	private State state = State.HEADER_START;

	private int msgType = -1;
	private long timeStamp = -1;
	private int length = 0;

	private final Charset charset;

	public OmcMessageDecoder() {
		// this(Charset.defaultCharset());
		this(Charset.forName("UTF-8"));
	}

	public OmcMessageDecoder(String charsetName) {
		if (Charset.isSupported(charsetName)) {
			this.charset = Charset.forName(charsetName);
		} else {
			this.charset = Charset.forName("UTF-8");
		}
	}

	public OmcMessageDecoder(Charset charset) {
		if (charset == null) {
			throw new NullPointerException("charset");
		}
		this.charset = charset;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (state != State.HEADER_END) {
			if (!readHeader(in)) {
				return;
			}
		}

		if (in.readableBytes() < length) {
			return;
		}

		// check, reset state if cause Exception?
		String content = in.readBytes(length).toString(charset);
		out.add(content.trim());

		reset();
	}

	private void reset() {
		state = State.HEADER_START;
		msgType = -1;
		timeStamp = -1;
		length = 0;
	}

	private boolean readHeader(ByteBuf in) {
		switch (state) {
			case HEADER_START:
				// wait until Header received
				if (in.readableBytes() < 9) {
					return false;
				}

				String header = getHexBinaryHeader(in);

				// read magic
				int magic0 = in.readUnsignedByte();
				if (magic0 != 255) {
					log.info("invalid header: " + header);
					return false;
				}

				int magic1 = in.readUnsignedByte();
				if (magic1 != 255) {
					log.info("invalid header: " + header);
					return false;
				}

				// read message type
				this.msgType = in.readUnsignedByte();

				// read time stamp
				this.timeStamp = in.readUnsignedInt();

				// read message body length 0~32767
				this.length = in.readUnsignedShort();

				log.info(String.format("header:%s, msgType:%d, timeStamp:%d, length:%d", header, msgType, timeStamp,
						length));

				state = State.HEADER_END;
			case HEADER_END:
				return true;
			default:
				throw new IllegalStateException();
		}
	}

	private String getHexBinaryHeader(ByteBuf buf) {
		byte[] bytes = new byte[9];
		int readerIndex = buf.readerIndex();
		buf.getBytes(readerIndex, bytes);
		return DatatypeConverter.printHexBinary(bytes);
	}

}
