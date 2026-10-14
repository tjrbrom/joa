package com.x.hhqs.joa.rt1agent.command.internal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.x.hhqs.joa.rt1agent.command.InternalCommand;
import com.x.hhqs.joa.rt1agent.command.RT1AgentResponseBuilder;
import com.x.hhqs.joa.rt1agent.forwarding.ponnbi.PonnbiClientManager;
import com.x.hhqs.joa.rt1agent.security.SecurityService;
import com.x.hhqs.joa.rt1agent.server.ContextUtil;
import com.x.hhqs.joa.rt1agent.server.SessionConnectionManager;
import com.x.hhqs.joa.rt1agent.server.SessionInfo;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import io.netty.channel.ChannelHandlerContext;

@Component
public class Logout extends InternalCommand {
	private static final Logger LOGGER = LoggerFactory.getLogger(Logout.class);

	private String commandCode = "LOGOUT";

	@Autowired
	private SessionConnectionManager connectionManager;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private RT1AgentResponseBuilder responseBuilder;
	
	@Autowired
	private PonnbiClientManager ponnbiClientManager;

	// LOGOUT:::CTAG::;
	@Override
	public Object execute(ChannelHandlerContext ctx, RT1InputMessage rt1msg) {
		processLogout(ctx);
		// ctx.channel().close();
		return responseBuilder.success(rt1msg);
	}

	private void processLogout(ChannelHandlerContext ctx) {
		SessionInfo sessionInfo = ContextUtil.getSession(ctx);

		if (sessionInfo == null) {
			return;
		}

		logoutClientSessionInfo(ctx, sessionInfo);
		LOGGER.info("Logout success from {}, user: {}, login: {}", ctx.channel().remoteAddress().toString(),
				sessionInfo.getAuthentication().getName(),
				LocalDateTime.ofInstant(Instant.ofEpochMilli(sessionInfo.getCreationTime()),
						ZoneId.systemDefault()));
	}

	private void logoutClientSessionInfo(ChannelHandlerContext ctx, SessionInfo sessionInfo) {
		sessionInfo.logout();
		ContextUtil.detachSession(ctx);
		connectionManager.removeClient(sessionInfo.getSessionId());
		securityService.logout(sessionInfo.getAuthentication());
		ponnbiClientManager.removeClient(sessionInfo.getSessionId());
	}

	@Override
	public String getCommandCode() {
		return commandCode;
	}

}
