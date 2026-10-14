package com.x.hhqs.joa.rt1agent.forwarding.ponnbi.handler;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rt1agent.forwarding.ponnbi.RT1ResponseWrapper;
import com.x.hhqs.joa.rt1agent.server.handler.RT1MessageDecoder;
import com.x.hhqs.joa.rt1message.RT1OutputMessage;
import com.x.hhqs.joa.rt1message.RT1ResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class PonnbiMessageDecoder extends MessageToMessageDecoder<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RT1MessageDecoder.class);

    public static int count = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        LOGGER.info("PONNBI response:\n" + msg);

        if (!StringUtils.hasText(msg)) {
            return;
        }
        try {
            RT1OutputMessage outMessage = RT1OutputMessage.parseOutputString(msg);
            if (outMessage == null) {
                LOGGER.error("Parse PONNBI response fail.");
                return;
            }

            if (outMessage.getType() != RT1OutputMessage.RESPONSE_MESSAGE) {
                LOGGER.warn("Discarded response due to unexpected type: {}", outMessage.getType());
                return;
            }

            RT1ResponseMessage response = (RT1ResponseMessage) outMessage;
            String ctag = response.getResponseID().getCTag();
            out.add(new RT1ResponseWrapper(msg, ctag, response));
        } catch (Exception e) {
            LOGGER.error("Parse PONNBI response fail.", e);
        }
    }
}
