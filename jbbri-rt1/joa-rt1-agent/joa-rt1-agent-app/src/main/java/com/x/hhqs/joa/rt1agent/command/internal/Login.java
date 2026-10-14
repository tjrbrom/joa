package com.x.hhqs.joa.rt1agent.command.internal;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rt1agent.command.EaddCode;
import com.x.hhqs.joa.rt1agent.command.EmptyResponse;
import com.x.hhqs.joa.rt1agent.command.InternalCommand;
import com.x.hhqs.joa.rt1agent.command.InternalCommand.Anonymous;
import com.x.hhqs.joa.rt1agent.command.RT1AgentResponseBuilder;
import com.x.hhqs.joa.rt1agent.security.Authentication;
import com.x.hhqs.joa.rt1agent.security.SecurityService;
import com.x.hhqs.joa.rt1agent.server.ContextUtil;
import com.x.hhqs.joa.rt1agent.server.SessionConnectionManager;
import com.x.hhqs.joa.rt1agent.server.SessionInfo;
import com.x.hhqs.joa.rt1agent.util.Rt1MessageUtil;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import io.netty.channel.ChannelHandlerContext;

@Component
@Anonymous
public class Login extends InternalCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Login.class);

    private String commandCode = "LOGIN";

    @Autowired
    private SessionConnectionManager connectionManager;
    //
    @Autowired
    private SecurityService securityService;

    @Autowired
    private RT1AgentResponseBuilder responseBuilder;

    // e.g. LOGIN:::CTAG::UN=admin,PWD=123456;
    @Override
    public Object execute(ChannelHandlerContext ctx, RT1InputMessage rt1msg) {
        if (isPonnbiForwardingLogin(rt1msg)) {
            LOGGER.warn("reject PONNBI forwarding login, sessionId {}", rt1msg.getCTag());
            ctx.close();
            return EmptyResponse.Instance;
        }

        if (isDuplicateLogin(ctx)) {
            return responseBuilder.response(rt1msg, EaddCode.ALREADY_LOGIN);
        }

        Properties params = Rt1MessageUtil.generateParameters(rt1msg);
        String username = getUsername(params);
        String password = getPassword(params);

        Authentication authentication = securityService.login(username, password);
        if (StringUtils.hasText(authentication.getErrorMessage())) {
            return responseBuilder.exceptionResponse(rt1msg, authentication.getErrorMessage());
        }

        if (!authentication.isValid()) {
            return responseBuilder.response(rt1msg, EaddCode.LOGIN_FAILURE);
        }

        String sessionId = initializeClientSessionInfo(ctx, authentication);

        LOGGER.info("Login success from {}, user: {}, sessionId: {}", ctx.channel().remoteAddress(), username,
            sessionId);
        return responseBuilder.success(rt1msg);
    }

    boolean isPonnbiForwardingLogin(RT1InputMessage rt1msg) {
        return connectionManager.getClient(rt1msg.getCTag()) != null;
    }

    private String initializeClientSessionInfo(ChannelHandlerContext ctx, Authentication authentication) {
        SessionInfo sessionInfo = SessionInfo.newLoginSessionInfo(authentication);
        //
        ContextUtil.attachSession(ctx, sessionInfo);
        connectionManager.addClient(sessionInfo.getSessionId(), ctx);

        return sessionInfo.getSessionId();
    }

    protected boolean isDuplicateLogin(ChannelHandlerContext ctx) {
        SessionInfo sessionInfo = ContextUtil.getSession(ctx);

        if (sessionInfo == null) {
            return false;
        }

        return sessionInfo.isLogin();
    }

    @Override
    public String getCommandCode() {
        return commandCode;
    }

    public String getUsername(Properties params) {
        return params.getProperty("UN");
    }

    public String getPassword(Properties params) {
        return params.getProperty("PWD");
    }

}
