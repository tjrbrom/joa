//package com.x.hhqs.joa.alarm.netty.server.handle;
//
//import com.x.hhqs.joa.alarm.netty.model.ClientSession;
//import com.x.hhqs.joa.alarm.netty.server.ChannelUtil;
//import com.x.hhqs.joa.alarm.netty.service.KeyCloakClientService;
//import com.x.hhqs.joa.alarm.netty.vo.KeycloakTokenVo;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.handler.timeout.IdleState;
//import io.netty.handler.timeout.IdleStateEvent;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.ObjectUtils;
//
//@Slf4j
//public class UpdateAmsSessionHandler extends ChannelInboundHandlerAdapter {
//
//	private KeyCloakClientService keyCloakClientService;
//
//	public UpdateAmsSessionHandler(){
//
//	}
//
//	public UpdateAmsSessionHandler(KeyCloakClientService keyCloakClientService) {
//		this.keyCloakClientService = keyCloakClientService;
//	}
//
//
//	private static final long SESSION_UPDATE_INTERVAL = 30 * 1000l;
//	private long lastUpdateTime = -1l;
//
//	@Override
//	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//		if (evt instanceof IdleStateEvent) {
//			IdleStateEvent e = (IdleStateEvent) evt;
//			if (e.state() == IdleState.READER_IDLE) {
////				updateAmsUserSession(ctx.channel());
//			}
//		}
//	}
//
//	private void updateAmsUserSession(Channel ch) {
//		if (!isLogin(ch)) {
//			return;
//		}
//		try {
//			String user = ChannelUtil.getSession(ch).getUserName();
//			ClientSession session = ChannelUtil.getSession(ch);
//			KeycloakTokenVo keycloakTokenVo = keyCloakClientService.loginACKeycloakToken(user, session.getPassword());
//			if(ObjectUtils.isNotEmpty(keycloakTokenVo)){
//				session.setLoginUser(keycloakTokenVo);
//				ChannelUtil.attachSession(ch, session);
//				log.debug("update ams user session success! username {} ,clientType {}",session.getClientType(), session.getUserName());
//			}
//			this.lastUpdateTime = System.currentTimeMillis();
//		} catch (Exception e) {
//			log.error("update ams user session fail!",e);
//		}
//	}
//
//	private boolean isLogin(Channel ch) {
//		return ChannelUtil.getSession(ch) != null;
//	}
//
//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		log.info("heart channelRead "+ lastUpdateTime + msg);
//		if ((System.currentTimeMillis() - lastUpdateTime) > SESSION_UPDATE_INTERVAL) {
//			updateAmsUserSession(ctx.channel());
//		}
//		ctx.fireChannelRead(msg);
//	}
//}
