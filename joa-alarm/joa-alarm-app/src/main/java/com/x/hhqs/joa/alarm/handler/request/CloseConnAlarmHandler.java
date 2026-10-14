package com.x.hhqs.joa.alarm.handler.request;

import com.x.hhqs.joa.alarm.handler.RequestMessageHandler;
import com.x.hhqs.joa.alarm.model.MessageType;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import com.x.hhqs.joa.alarm.server.BroadcastServer;
import com.x.hhqs.joa.alarm.server.handler.OmcAlarmChannelIndexFilter;
import com.x.hhqs.joa.alarm.server.handler.OmcAlarmHandler;
import com.x.hhqs.joa.alarm.service.OmcUserManagementService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class CloseConnAlarmHandler extends RequestMessageHandler {

	@Autowired
	private OmcUserManagementService userManagementService;

	@Resource
	private BroadcastServer broadcastServer;

	@Override
	public void executeRequest(Channel ch, OmcRequest request) {
		userManagementService.logout(ch);
		try {
			broadcastServer.removeChannel(ch);
			ch.pipeline().remove(OmcAlarmHandler.NAME);
			ch.pipeline().remove(OmcAlarmChannelIndexFilter.NAME);
		} catch (Exception ignored) {

		} finally {
			ch.close();
		}
	}

	@Override
	public MessageType getType() {
		return MessageType.closeConnAlarm;
	}

}
