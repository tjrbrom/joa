package com.x.hhqs.joa.rt1agent.forwarding.ponnbi;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.x.hhqs.joa.rt1agent.command.EaddCode;
import com.x.hhqs.joa.rt1agent.command.FailResultException;
import com.x.hhqs.joa.rt1agent.forwarding.ponnbi.handler.LoginResponseHandler;
import com.x.hhqs.joa.rt1agent.forwarding.ponnbi.handler.PonnbiResponseHandler;
import com.x.hhqs.joa.rt1agent.server.SessionConnectionManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;

public class PonnbiClientManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PonnbiClientManager.class);

    public static final AttributeKey<Boolean> LOGIN_SUCCESS = AttributeKey.valueOf("LOGIN_SUCCESS");
    public static final AttributeKey<String> SESSION_ID = AttributeKey.valueOf("SESSION_ID");

    public static final String LOGIN_HANDLER = "loginHandler";

    private final Bootstrap bootstrap;
    private final EventLoopGroup group = new NioEventLoopGroup();

    private final Map<String, PonnbiClient> clients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    private PonnbiProperties ponnbiProperties;
    
    @Autowired
    private SessionConnectionManager joaConnectionManager;

    public PonnbiClientManager(PonnbiProperties ponnbiProperties, Bootstrap bootstrap) {
        this.ponnbiProperties = ponnbiProperties;
        this.bootstrap = bootstrap;
    }

    @PostConstruct
    public void start() throws InterruptedException {
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ponnbiProperties.getConnectTimeout())
            .handler(new LoggingHandler(LogLevel.INFO))
            .handler(new PonnbiChannelInitializer(ponnbiProperties));
    }

    @PreDestroy
    public void stop() {
        for (PonnbiClient client : clients.values()) {
            if (client.isActive()) {
                client.close();
            }
        }
        group.shutdownGracefully();
    }

    public PonnbiClient getClient(String sessionId) throws Exception {
        ReentrantLock lock = lockMap.computeIfAbsent(sessionId, k -> new ReentrantLock());
        lock.lock();

        try {
            PonnbiClient client = clients.get(sessionId);
            if (client != null && client.isLoggedIn()) {
                return client;
            }

            if (client != null) {
                client.close();
                removeClient(sessionId);
            }

            Channel channel = createChannel(sessionId);
            if (channel == null) {
                throw new FailResultException(EaddCode.PONNBI_CONNECT_FAIL);
            }
            
            PonnbiClient newClient = new PonnbiClient(channel, sessionId);
            ChannelHandlerContext joaCtx = getJoaConnectionManager().getClient(sessionId);
            newClient.setJoaUserCtx(joaCtx);
            
            clients.put(sessionId, newClient);
            channel.pipeline().addLast(new PonnbiResponseHandler(this, newClient));

            return newClient;
        } finally {
            lock.unlock();
        }
    }

    Channel createChannel(String sessionId) {
        try {
            ChannelFuture future = bootstrap.connect(ponnbiProperties.getHost(),
                ponnbiProperties.getPort()).sync();

            // connect failed
            if (!future.isSuccess()) {
                LOGGER.error("Connect PONNBI {}:{} failed.", ponnbiProperties.getHost(), ponnbiProperties.getPort());
                try {
                    LOGGER.error(future.cause().getMessage(), future.cause());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
                return null;
            }

            Channel channel = future.channel();
            if (login(channel, sessionId)) {
                return channel;
            }
            // login failed, close channel
            channel.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    boolean login(Channel channel, String sessionId) {
        try {
            channel.attr(LOGIN_SUCCESS).set(false);
            channel.attr(SESSION_ID).set(sessionId);

            CompletableFuture<Boolean> loginFuture = createLoginFuture();
            channel.pipeline().addBefore(PonnbiChannelInitializer.MESSAGE_DECODER, LOGIN_HANDLER,
                new LoginResponseHandler(loginFuture));

            String command = String.format("LOGIN:::%s::UN=%s,PWD=%s;", sessionId,
                ponnbiProperties.getUsername(), ponnbiProperties.getPassword());
            channel.writeAndFlush(command);

            if (loginFuture.get(5, TimeUnit.SECONDS)) {
                channel.attr(LOGIN_SUCCESS).set(true);
                channel.pipeline().remove(LOGIN_HANDLER);
                return true;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
    
    CompletableFuture<Boolean> createLoginFuture() {
        return new CompletableFuture<>();
    }

    public void removeClient(String sessionId) {
        PonnbiClient client = clients.remove(sessionId);
        if (client != null) {
            client.setJoaUserCtx(null);
            client.close();
        }
    }

    public SessionConnectionManager getJoaConnectionManager() {
        return joaConnectionManager;
    }
}
