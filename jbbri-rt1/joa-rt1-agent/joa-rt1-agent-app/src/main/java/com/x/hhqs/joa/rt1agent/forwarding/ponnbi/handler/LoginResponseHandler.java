package com.x.hhqs.joa.rt1agent.forwarding.ponnbi.handler;

import static com.x.hhqs.joa.rt1agent.forwarding.ponnbi.PonnbiClientManager.LOGIN_SUCCESS;
import static com.x.hhqs.joa.rt1agent.forwarding.ponnbi.PonnbiClientManager.SESSION_ID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.x.hhqs.joa.rt1message.RT1OutputMessage;
import com.x.hhqs.joa.rt1message.RT1ResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class LoginResponseHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResponseHandler.class);

    private final CompletableFuture<Boolean> loginFuture;

    public LoginResponseHandler(CompletableFuture<Boolean> loginFuture) {
        this.loginFuture = loginFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        LOGGER.info("msg:\n" + msg.trim());

        // should not happen
        if (ctx.channel().attr(LOGIN_SUCCESS).get()) {
            LOGGER.error("LoginHandler should be removed after login success!");
            return;
        }

        try {
            RT1OutputMessage out = parseOutputMessage(msg);
            if (out.getType() == RT1OutputMessage.RESPONSE_MESSAGE) {
                RT1ResponseMessage response = (RT1ResponseMessage) out;
                String sessionId = ctx.channel().attr(SESSION_ID).get();
                if (!response.getResponseID().getCTag().equals(sessionId)) {
                    return;
                }
                if (RT1ResponseMessage.COMPLETED.equalsIgnoreCase(response.getResponseID().getCompletionCode())) {
                    loginFuture.complete(true);
                }
                loginFuture.complete(false);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    RT1OutputMessage parseOutputMessage(String message) throws Exception {
        return RT1OutputMessage.parseOutputString(message);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        loginFuture.complete(false);
        super.channelInactive(ctx);
    }

}

