package com.x.hhqs.joa.alarm.server.handler;

import com.google.common.base.Strings;
import com.x.hhqs.joa.alarm.api.CommandHandler;
import com.x.hhqs.joa.alarm.api.CommandHandlerRegistry;
import com.x.hhqs.joa.alarm.handler.LoginFailCountsManage;
import com.x.hhqs.joa.alarm.handler.request.Anonymous;
import com.x.hhqs.joa.alarm.model.ClientSession;
import com.x.hhqs.joa.alarm.model.MessageType;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import com.x.hhqs.joa.alarm.model.response.OmcAckMessage;
import com.x.hhqs.joa.alarm.server.ChannelUtil;
import com.x.hhqs.joa.alarm.service.OmcUserManagementService;
import com.x.hhqs.joa.common.core.contants.MessageConstant;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import com.x.hhqs.joa.alarm.property.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class OmcRequestHandler extends SimpleChannelInboundHandler<OmcRequest> {

	@Autowired
	private CommandHandlerRegistry handlerRegistry;

	@Autowired
	private OmcUserManagementService userManagementService;

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, OmcRequest request) {
		try {
			// 1.check if command is support
			CommandHandler handler = getCommandHandler(request);

			if (handler == null) {
				log.warn("Cannot find CommandHandler to execute request: " + request);
				fail(ctx, request, Messages.getString("resDesc.unSupportedCommand"));
				return;
			}

			// 2.execute command which can be executed without login
			if (isAnonymous(handler)) {
				handler.execute(ctx.channel(), request);
				return;
			}

			// 3.check if client has login
			if (clientNotLogin(ctx)) {
				log.warn("Client is not logged in.");
				fail(ctx, request, Messages.getString("resDesc.unlogin"));
				return;
			}

			// 5.execute command
			handler.execute(ctx.channel(), request);

		} catch (Exception e) {
			log.error("",e);
			fail(ctx, request, Messages.getString("resDesc.executeFail"));
		}
	}

	private void fail(final ChannelHandlerContext ctx, OmcRequest request, String resDesc) {
		MessageType responseType = request.getMessageType().getResponseType();
		OmcAckMessage ackMessage = OmcAckMessage.getAckMessage(responseType);

		String reqId = request.getValue("ReqId");
		if (Strings.isNullOrEmpty(reqId)) {
			reqId = "-1";
		}

		ackMessage.setValue("reqId", reqId);
		ackMessage.setValue("result", MessageConstant.FAIL);
		ackMessage.setValue("resDesc", resDesc);

		ctx.writeAndFlush(ackMessage);
	}

	private boolean clientNotLogin(ChannelHandlerContext ctx) {
		return !isLogin(ctx.channel());
	}

	private boolean isLogin(Channel ch) {
		return ChannelUtil.getSession(ch) != null;
	}

	private CommandHandler getCommandHandler(OmcRequest request) {
		String commandName = request.getMessageType().getMessageName().toLowerCase();
        return handlerRegistry.getCommandHandler(commandName);
	}

	public Boolean isAnonymous(CommandHandler handler) {
		Class<?> clazz = handler.getClass();
		return clazz.isAnnotationPresent(Anonymous.class);
	}
	
	/**
	 * If channel inactive without logout, remove session
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("# channelInactive: {}", ctx.channel().remoteAddress());
		ClientSession session = ChannelUtil.getSession(ctx.channel());
		if (session != null) {
			userManagementService.logout(ctx.channel());
			log.warn("Client {} inactive without logout, remove session.", ctx.channel().remoteAddress().toString());
		} else {
			LoginFailCountsManage.removeLoginFailCounts(ctx.channel());
			log.warn("remove loginFailCounts channel channel id {}", ctx.channel().id());
		}
		super.channelInactive(ctx);
	}

}
