package com.x.hhqs.joa.rt1agent.server;

import com.x.hhqs.joa.rt1agent.config.ConnectionConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.x.hhqs.joa.rt1agent.server.handler.ConnectionLimitHandler;
import com.x.hhqs.joa.rt1agent.server.handler.FailResultMessageEncoder;
import com.x.hhqs.joa.rt1agent.server.handler.RemoteAddressFilter;
import com.x.hhqs.joa.rt1agent.server.handler.RT1MessageDecoder;
import com.x.hhqs.joa.rt1agent.server.handler.RT1MessageDispatcher;
import com.x.hhqs.joa.rt1agent.server.handler.RT1StringDecoder;
import com.x.hhqs.joa.rt1agent.server.handler.RT1StringEncoder;
import com.x.hhqs.joa.rt1agent.server.handler.TimeoutHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

@Component
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerInitializer.class);

	private static final TimeoutHandler timeoutHandler = new TimeoutHandler();

	@Autowired
	private ConnectionLimitHandler connectionLimitHandler;

	@Autowired
	private RemoteAddressFilter ipFilter;

	@Autowired
	private RT1MessageDispatcher rt1MessageDispatcher;

	@Autowired
	private RT1MessageDecoder rt1MessageDecoder;
	
	@Autowired
	private FailResultMessageEncoder failResultMessageEncoder;

	private int maxFrameLength = 8192;

	@Autowired
	private ConnectionConfigurationProperties connectionProperties;

	public NettyServerInitializer() {
		// Do nothing
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		LOGGER.info("Client connected from " + ch.remoteAddress());
		LOGGER.info("idleTimeoutMinutes: {}", connectionProperties.getIdleTimeoutMinutes());

		ChannelPipeline pipeline = ch.pipeline();

		// handler ipAddress filter
		pipeline.addLast(ipFilter);

		// handler connection limit
		pipeline.addLast(connectionLimitHandler);

		// handler timeout
		pipeline.addLast(new IdleStateHandler(0, 0, connectionProperties.getIdleTimeoutMinutes() * 60));
		pipeline.addLast(timeoutHandler);

		// Add the text line codec combination
		pipeline.addLast(new DelimiterBasedFrameDecoder(maxFrameLength, false, lineDelimiter()));

		// the encoder and decoder
		pipeline.addLast(new RT1StringDecoder(connectionProperties.getEncoding()));
		pipeline.addLast(new RT1StringEncoder(connectionProperties.getEncoding()));
		pipeline.addLast(failResultMessageEncoder);

		// and then business logic.
		pipeline.addLast(rt1MessageDecoder);
		pipeline.addLast(rt1MessageDispatcher);
	}

	private static ByteBuf[] lineDelimiter() {
		return new ByteBuf[] { Unpooled.wrappedBuffer(new byte[] { ';' }) };
	}

}
