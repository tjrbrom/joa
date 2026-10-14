package com.x.hhqs.joa.alarm.handler.request;


import com.x.hhqs.joa.alarm.handler.LoginFailCountsManage;
import com.x.hhqs.joa.alarm.logger.AlarmLogger;
import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.handler.RequestMessageHandler;
import com.x.hhqs.joa.alarm.model.ClientSession;
import com.x.hhqs.joa.alarm.model.ClientType;
import com.x.hhqs.joa.alarm.model.MessageType;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import com.x.hhqs.joa.alarm.model.OperateResult;
import com.x.hhqs.joa.alarm.model.response.OmcAckMessage;
import com.x.hhqs.joa.alarm.server.BroadcastServer;
import com.x.hhqs.joa.alarm.server.ChannelUtil;
import com.x.hhqs.joa.alarm.server.handler.OmcAlarmChannelIndexFilter;
import com.x.hhqs.joa.alarm.server.handler.OmcAlarmHandler;
import com.x.hhqs.joa.alarm.service.OmcUserManagementService;
import com.x.hhqs.joa.common.core.contants.MessageConstant;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import com.x.hhqs.joa.alarm.property.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;


@Component
@Anonymous
@Slf4j
public class ReqLoginAlarmHandler extends RequestMessageHandler {

	@Autowired
	private OmcUserManagementService userManagementService;

	@Autowired
	private BroadcastServer broadcastServer;

	@Resource
	private JoaAlarmConfig joaAlarmConfig;

	@Override
	public void executeRequest(Channel ch, OmcRequest request) {
		// 1. check client connect type
		ClientType clientType = ClientType.getCientType(request.getValue("type"));
		if (clientType == ClientType.unSupport) {
			fail(ch, invalidParamter("type"));
			return;
		}

		// 2. check duplicate login
		ClientSession session = ChannelUtil.getSession(ch);
		if (session != null) {
			fail(ch, Messages.getString("resDesc.alreadyLogin"));
			return;
		}

		// 3. login
		String username = request.getValue("user").toLowerCase();
		String password = request.getValue("key");
		OperateResult result = userManagementService.login(username, password, clientType, ch);
		if (!result.isSuccess()) {
			fail(ch, result.getErrorMessage());
			LoginFailCountsManage.checkLoginFailTimes(ch);
			return;
		}
		LoginFailCountsManage.removeLoginFailCounts(ch);

		// 4. receive real alarm
		int port = ((InetSocketAddress) ch.localAddress()).getPort();
		if (ClientType.msg == clientType) {
			ch.pipeline().addLast(OmcAlarmHandler.NAME,
					new OmcAlarmHandler(joaAlarmConfig.getCharset(), username, port));
			ch.pipeline().addLast(OmcAlarmChannelIndexFilter.NAME,
					new OmcAlarmChannelIndexFilter(getChannelIndex(ch)));
			broadcastServer.addChannel(ch);
			//record user login log
			AlarmLogger.userLogin(username, port, ch.remoteAddress());
		}

		// 5. notify client login success
        log.info("Login success from {}, user={}, type={}, port={}", ch.remoteAddress(), username, clientType, port);
		success(ch);
	}

	private int getChannelIndex(Channel ch) {
		int port = ((InetSocketAddress) ch.localAddress()).getPort();
		return port - joaAlarmConfig.getServer().getPort();
	}

	private void success(Channel ch) {
		OmcAckMessage ackMessage = getOmcAckMessage();
		ackMessage.setValue("result", MessageConstant.SUCC);
		ch.writeAndFlush(ackMessage);
	}

	private void fail(Channel ch, String resDesc) {
		OmcAckMessage ackMessage = getOmcAckMessage();
		ackMessage.setValue("result", MessageConstant.FAIL);
		ackMessage.setValue("resDesc", resDesc);
		ch.writeAndFlush(ackMessage);
	}

	@Override
	public MessageType getType() {
		return MessageType.reqLoginAlarm;
	}

}

