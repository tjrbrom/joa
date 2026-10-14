package com.x.hhqs.joa.rt1agent.forwarding.ponnbi.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.x.hhqs.joa.rt1agent.forwarding.ponnbi.PonnbiClient;
import com.x.hhqs.joa.rt1agent.forwarding.ponnbi.PonnbiClientManager;
import com.x.hhqs.joa.rt1agent.forwarding.ponnbi.RT1ResponseWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PonnbiResponseHandler extends SimpleChannelInboundHandler<RT1ResponseWrapper> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PonnbiResponseHandler.class);

    private PonnbiClientManager clientManager;
    private PonnbiClient client;

    public PonnbiResponseHandler(PonnbiClientManager clientManager, PonnbiClient client) {
        this.clientManager = clientManager;
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RT1ResponseWrapper responseWrapper) {
        client.writeToJoaChannel(responseWrapper);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        client.connectionLost();
        clientManager.removeClient(client.getSessionId());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("[exceptionCaught][connect({}) exception occurs]", ctx.channel().id(), cause);
    }
}
