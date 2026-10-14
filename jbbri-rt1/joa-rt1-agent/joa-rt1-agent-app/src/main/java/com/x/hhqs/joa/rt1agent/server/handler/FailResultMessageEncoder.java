package com.x.hhqs.joa.rt1agent.server.handler;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.x.hhqs.joa.rt1agent.command.FailResultMessage;
import com.x.hhqs.joa.rt1agent.command.RT1AgentResponseBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageEncoder;

@Sharable
@Component
public class FailResultMessageEncoder extends MessageToMessageEncoder<FailResultMessage> {

    @Autowired
    private RT1AgentResponseBuilder responseBuilder;

    @Override
    protected void encode(ChannelHandlerContext ctx, FailResultMessage result, List<Object> out) throws Exception {
        out.add(responseBuilder.response(result.getRt1Message(), result.getFailResult()).toString());
    }
}
