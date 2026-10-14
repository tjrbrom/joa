package com.x.hhqs.joa.rt1agent.forwarding.ponnbi;

import static com.x.hhqs.joa.rt1agent.forwarding.ponnbi.PonnbiClientManager.LOGIN_SUCCESS;
import java.util.List;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ArrayListMultimap;
import com.x.hhqs.joa.rt1agent.command.EaddCode;
import com.x.hhqs.joa.rt1agent.command.FailResultMessage;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class PonnbiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(PonnbiClient.class);

    private String sessionId;

    private Channel channel;

    private ChannelHandlerContext joaUserCtx;

    private ArrayListMultimap<String, RT1InputMessage> commandCache = ArrayListMultimap.create();

    private boolean isConnectionLost = false;

    public PonnbiClient(Channel channel, String sessionId) {
        this.channel = channel;
        this.sessionId = sessionId;
    }

    public boolean isActive() {
        return channel != null && channel.isActive() && !isConnectionLost;
    }

    public boolean isLoggedIn() {
        return isActive() && channel.attr(LOGIN_SUCCESS).get();
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public synchronized void connectionLost() {
        isConnectionLost = true;
        if (joaUserCtx == null) {
            return;
        }

        for (Entry<String, RT1InputMessage> entry : commandCache.entries()) {
            LOGGER.info("PONNBI connection lost. Command failed. [{}]", entry.getValue());
            joaUserCtx.channel().writeAndFlush(new FailResultMessage(entry.getValue(), EaddCode.PONNBI_CONNECTION_LOST));
        }
        commandCache.clear();
    }

    public synchronized void write(RT1InputMessage message) {
        if (isConnectionLost) {
            LOGGER.info("PONNBI connection lost. Command failed. [{}]", message.toString());
            joaUserCtx.channel().writeAndFlush(new FailResultMessage(message, EaddCode.PONNBI_CONNECTION_LOST));
            return;
        }
        commandCache.put(message.getCTag(), message);
        channel.writeAndFlush(message.toString());
    }

    public synchronized void writeToJoaChannel(RT1ResponseWrapper responseWrapper) {
        List<RT1InputMessage> list = commandCache.get(responseWrapper.getCtag());
        if (list.isEmpty()) {
            return;
        } else {
            list.remove(0);
        }

        joaUserCtx.channel().writeAndFlush(responseWrapper.getRawResponse());
    }

    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void setJoaUserCtx(ChannelHandlerContext joaUserCtx) {
        this.joaUserCtx = joaUserCtx;
    }

    public ChannelHandlerContext getJoaUserCtx() {
        return joaUserCtx;
    }

}
