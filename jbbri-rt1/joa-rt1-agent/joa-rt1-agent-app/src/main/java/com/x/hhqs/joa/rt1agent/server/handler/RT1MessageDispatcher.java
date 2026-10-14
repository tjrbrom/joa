package com.x.hhqs.joa.rt1agent.server.handler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rt1agent.command.CommandExecutor;
import com.x.hhqs.joa.rt1agent.forwarding.ponnbi.PonnbiClientManager;
import com.x.hhqs.joa.rt1agent.security.SecurityService;
import com.x.hhqs.joa.rt1agent.server.ContextUtil;
import com.x.hhqs.joa.rt1agent.server.SessionConnectionManager;
import com.x.hhqs.joa.rt1agent.server.SessionInfo;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Component
@Sharable
public class RT1MessageDispatcher extends SimpleChannelInboundHandler<RT1InputMessage> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RT1MessageDispatcher.class);

	@Autowired
	private CommandExecutor commandExecutor;

	@Autowired
	private SessionConnectionManager connectionManager;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
    private PonnbiClientManager ponnbiClientManager;

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, RT1InputMessage msg) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("## RT1InputMessage: {}", msg);
		}

		Object responseMessage = commandExecutor.execute(ctx, msg);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("## RT1ResponseMessage: {}", responseMessage);
		}

		sendResponse(ctx, responseMessage);
	}

	private void sendResponse(final ChannelHandlerContext ctx, Object responseMessage) throws Exception {
		if (responseMessage == null) {
			return;
		}
		
		if (responseMessage instanceof Collection) {
		    ((Collection<?>) responseMessage).stream()
            .filter(s -> StringUtils.hasText(s.toString()))
            .forEach(s -> ctx.writeAndFlush(s.toString()));
            return;
        }

		if (!StringUtils.hasText(responseMessage.toString())) {
			return;
		}

		ctx.writeAndFlush(responseMessage.toString());
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	/**
	 * If channel inactive without logout, remove session
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("# channelInactive {}", ctx.channel().remoteAddress().toString());

		SessionInfo sessionInfo = ContextUtil.getSession(ctx);
		if (sessionInfo != null) {
			
			logoutClientSessionInfo(ctx, sessionInfo);
			LOGGER.warn("Client {} inactive without logout, remove session. user: {}, login: {}",
					ctx.channel().remoteAddress().toString(), sessionInfo.getAuthentication().getName(),
					LocalDateTime.ofInstant(Instant.ofEpochMilli(sessionInfo.getCreationTime()),
							ZoneId.systemDefault()));
		}

		super.channelInactive(ctx);
	}

	private void logoutClientSessionInfo(ChannelHandlerContext ctx, SessionInfo sessionInfo) {
		sessionInfo.logout();
		ContextUtil.detachSession(ctx);
		connectionManager.removeClient(sessionInfo.getSessionId());
		securityService.logout(sessionInfo.getAuthentication());
		ponnbiClientManager.removeClient(sessionInfo.getSessionId());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		LOGGER.error("[exceptionCaught][connect({}) exception occurs]", ctx.channel().id(), cause);
		// ctx.channel().close();
		// close() will cause channelInactive() then channelUnregistered()
	}

}
