package com.x.hhqs.joa.alarm.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Component
@Slf4j
public class NettyServer implements InitializingBean, DisposableBean {

    @Autowired
    private NettyServerInitializer nettyServerInitializer;

    private EventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

//    @Resource
//    private JoaAlarmConfig joaAlarmConfig;

    @Value("${joa-alarm.server.port}")
    private Integer serverPort;

    @Value("${joa-alarm.server.channel-number}")
    private Integer channelNumber;

    private final CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<>();

    @Override
    public void afterPropertiesSet() {
        log.info("netty init.");
        start();
    }

    public synchronized void start() {
        log.info("netty start.");
        if (channelNumber < 1 || channelNumber > 40) {
            log.error("channelNumber {} out of range: ", channelNumber);
            throw new RuntimeException("channelNumber out of range");
        }
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = createServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(nettyServerInitializer);
        IntStream.range(serverPort, serverPort + channelNumber)
                .forEach(port -> {
                    log.info("start joa alarm server on port {} ", port);
                    try {
                        Channel channel = serverBootstrap.bind(port).sync().channel();
                        channels.add(channel);
                    } catch (Exception e) {
                        log.error("port {} start error!", port, e);
                        shutdown();
                        throw new RuntimeException("netty server start error!");
                    }
                });
        log.info("joa alarm server started.");
    }

    @Override
    public void destroy() {
        shutdown();
    }

    private void shutdown() {
        log.info("stop OAS server...");

        List<Channel> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(channels);
        }
        snapshot.parallelStream()
                .forEach(channel -> {
                    try {
                        if (channel.isActive()) {
                            channel.close().sync();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted while closing channel {}", channel.id());
                    }
                });
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> bossGroup.shutdownGracefully(5, 10, TimeUnit.SECONDS)),
                CompletableFuture.runAsync(() -> workerGroup.shutdownGracefully(5, 10, TimeUnit.SECONDS))
        ).exceptionally(ex -> {
            log.error("shutdown failed, forcing termination", ex);
            bossGroup.shutdownNow();
            workerGroup.shutdownNow();
            return null;
        }).join();
        channels.clear();
    }

    public ServerBootstrap createServerBootstrap() {
       return new ServerBootstrap();
    }
}
