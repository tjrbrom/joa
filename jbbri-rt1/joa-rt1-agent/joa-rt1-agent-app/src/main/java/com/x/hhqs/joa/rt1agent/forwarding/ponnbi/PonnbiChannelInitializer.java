package com.x.hhqs.joa.rt1agent.forwarding.ponnbi;

import com.x.hhqs.joa.rt1agent.forwarding.ponnbi.handler.PonnbiMessageDecoder;
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
import io.netty.util.CharsetUtil;

public class PonnbiChannelInitializer extends ChannelInitializer<SocketChannel> {
    public static final String MESSAGE_DECODER = "ponnbiMessageDecoder";

    private static final TimeoutHandler timeoutHandler = new TimeoutHandler();

    private int maxFrameLength = 8192;

    private PonnbiProperties ponnbiProperties;

    public PonnbiChannelInitializer(PonnbiProperties ponnbiProperties) {
        this.ponnbiProperties = ponnbiProperties;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // handler timeout
        if (ponnbiProperties.getIdleTimeoutMinutes() != -1) {
            pipeline.addLast(new IdleStateHandler(0, 0, ponnbiProperties.getIdleTimeoutMinutes() * 60));
            pipeline.addLast(timeoutHandler);
        }

        // Add the text line codec combination
        pipeline.addLast(new DelimiterBasedFrameDecoder(maxFrameLength, false, lineDelimiter()));

        // the encoder and decoder
        pipeline.addLast(new RT1StringDecoder(ponnbiProperties.getEncoding()));
        pipeline.addLast(new RT1StringEncoder(ponnbiProperties.getEncoding()));

        // and then business logic.
        pipeline.addLast(MESSAGE_DECODER, new PonnbiMessageDecoder());
        // pipeline.addLast(new PonnbiResponseHandler());
    }

    static ByteBuf[] lineDelimiter() {
        return new ByteBuf[] {Unpooled.copiedBuffer("\r\n;", CharsetUtil.UTF_8),
                Unpooled.copiedBuffer("\r\n>", CharsetUtil.UTF_8),
                Unpooled.copiedBuffer("\r\n<", CharsetUtil.UTF_8)};
    }

}
