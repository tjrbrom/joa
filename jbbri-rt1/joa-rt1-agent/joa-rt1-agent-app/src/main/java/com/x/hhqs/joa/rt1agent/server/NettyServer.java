package com.x.hhqs.joa.rt1agent.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import com.x.hhqs.joa.rt1agent.config.ConnectionConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;

@Component
public class NettyServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

	@Resource
	private NettyServerInitializer nettyServerInitializer;

	private ServerBootstrap bootstrap;
	private EventLoopGroup bossGroup;
	private NioEventLoopGroup workerGroup;

	private Channel channel;

	@Autowired
	private ConnectionConfigurationProperties connectionProperties;

	@Value("${io.netty.eventLoopThreads:0}")
	private int eventLoopThreads = 0;

	@PostConstruct
	public void start() throws Throwable {
		LOGGER.info("# Start rt1agent on port {}", connectionProperties.getPort());
		LOGGER.info("# eventLoopThreads: {}", eventLoopThreads > 0 ? eventLoopThreads
				: SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));

		bootstrap = new ServerBootstrap();

		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup(eventLoopThreads);
		// NettyEventLoopFactory.eventLoopGroup(1, "NettyServerBoss");

		bootstrap.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.TCP_NODELAY, true)
				.childHandler(nettyServerInitializer);

		ChannelFuture channelFuture = bootstrap.bind(connectionProperties.getPort());
		channelFuture.syncUninterruptibly();
		channel = channelFuture.channel();
	}

	@PreDestroy
	public void stop() {
		LOGGER.info("# Stop rt1agent");
		try {
			if (channel != null) {
				channel.close();
			}
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}

		try {
			if (bootstrap != null) {
				bossGroup.shutdownGracefully().syncUninterruptibly();
				workerGroup.shutdownGracefully().syncUninterruptibly();
			}
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

}
