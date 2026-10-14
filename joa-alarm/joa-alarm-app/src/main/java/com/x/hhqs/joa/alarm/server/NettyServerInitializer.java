package com.x.hhqs.joa.alarm.server;


import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.server.handler.OmcMessageDecoder;
import com.x.hhqs.joa.alarm.server.handler.OmcMessageEncoder;
import com.x.hhqs.joa.alarm.server.handler.OmcRequestDecoder;
import com.x.hhqs.joa.alarm.server.handler.OmcRequestHandler;
import com.x.hhqs.joa.alarm.server.handler.TimeoutHandler;
//import com.x.hhqs.joa.alarm.netty.server.handle.UpdateAmsSessionHandler;
import com.x.hhqs.joa.alarm.service.KeyCloakClientService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

	private static final TimeoutHandler timeoutHandler = new TimeoutHandler();

	@Resource
	private OmcRequestDecoder omcRequestDecoder;

	@Resource
	private OmcRequestHandler omcRequestHandler;

	@Resource
	private KeyCloakClientService keyCloakClientService;

	@Resource
	private JoaAlarmConfig joaAlarmConfig;


	public NettyServerInitializer() {
		// Do nothing
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		//
		log.info("Client connected from " + ch.remoteAddress());

		ChannelPipeline pipeline = ch.pipeline();

		// handle timeout
		log.info("set timeoutMinutes: " + joaAlarmConfig.getClientIdleTimeOut());
		pipeline.addLast(new IdleStateHandler(joaAlarmConfig.getClientIdleTimeOut() * 60, 0, 0));
		pipeline.addLast(timeoutHandler);

		// handle ams session update
//		pipeline.addLast(new IdleStateHandler(30, 0, 0));
//		pipeline.addLast(new UpdateAmsSessionHandler(keyCloakClientService));

		log.info("set Charset: " + joaAlarmConfig.getCharset());
		pipeline.addLast(new OmcMessageDecoder(joaAlarmConfig.getCharset()));
		pipeline.addLast(new OmcMessageEncoder(joaAlarmConfig.getCharset()));

		pipeline.addLast(omcRequestDecoder);
		pipeline.addLast(omcRequestHandler);
	}

}
