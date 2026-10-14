package com.x.hhqs.joa.alarm.service.impl;


import com.x.hhqs.joa.alarm.logger.AlarmLogger;
import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.model.ClientSession;
import com.x.hhqs.joa.alarm.model.ClientType;
import com.x.hhqs.joa.alarm.model.OmcUser;
import com.x.hhqs.joa.alarm.model.OperateResult;
import com.x.hhqs.joa.alarm.model.login.LoginUser;
import com.x.hhqs.joa.alarm.server.ChannelUtil;
import com.x.hhqs.joa.alarm.service.KeyCloakClientService;
import com.x.hhqs.joa.alarm.service.OmcUserManagementService;
import com.x.hhqs.joa.alarm.service.manager.SyncAlarmFileChannelManage;
import com.x.hhqs.joa.alarm.utils.ConnectKey;
import com.x.hhqs.joa.alarm.vo.KeycloakTokenVo;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import com.x.hhqs.joa.alarm.property.Messages;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class OmcUserManagementServiceImpl implements OmcUserManagementService {

	@Autowired
	private JoaAlarmConfig joaAlarmConfig;

	private ConcurrentHashMap<ConnectKey, AtomicInteger> connectCounts = new ConcurrentHashMap<>();

	// Key: sessionId
	private ConcurrentHashMap<String, Channel> clients = new ConcurrentHashMap<>();

	@Resource
	private KeyCloakClientService keyCloakClientService;

	@Resource
	private SyncAlarmFileChannelManage syncAlarmFileChannelManage;

/*	@Override
	public boolean validateUser(String username, String password) {
		return false;
	}*/

	private int incrementAndGet(String username, ClientType clientType, int port) {
		return getConnectCount(username, clientType, port).incrementAndGet();
	}

	private int decrementAndGet(String username, ClientType clientType, int port) {
		AtomicInteger connect = getConnectCount(username, clientType, port);
		if (connect.get() == 0) {
			return 0;
		}
		return connect.decrementAndGet();
	}

	private AtomicInteger getConnectCount(String username, ClientType clientType, int port) {
		ConnectKey key = ConnectKey.getConnectKey(username, clientType, port);
		AtomicInteger count = connectCounts.get(key);

		if (count == null) {
			connectCounts.putIfAbsent(key, new AtomicInteger(0));
		}

		return connectCounts.get(key);
	}

	@Override
	public OmcUser getUser(String username) {
		return new OmcUser(username);
	}

/*	@Override
	public void logout(String sessionId) {
	}*/

	@Override
	public void logout(Channel ch) {
		ClientSession session = ChannelUtil.detachSession(ch);
		if (session == null) {
			return;
		}
		logout(session);
		log.info("User[{}, {}] logout on port {} from {}.", session.getUserName(), session.getClientType().name(), session.getPort(), ch.remoteAddress().toString());
		if (ClientType.msg == session.getClientType()) {
			AlarmLogger.userLogout(session.getUserName(), session.getPort(), ch.remoteAddress());
		} else {
			String userName =session.getUserName();
			String reqSyncId = String.format("%s_%s", userName , ((InetSocketAddress) ch.localAddress()).getPort());
			syncAlarmFileChannelManage.removeRequestFromStorage(reqSyncId);
		}
	}

	private void logout(ClientSession session) {
		decrementAndGet(session.getUserName(), session.getClientType(), session.getPort());
		clients.remove(session.getSessionId());
	}

	@Override
	public OperateResult login(String username, String password, ClientType clientType, Channel ch) {
		log.info("user {} login",username);
		if (username == null || username.isEmpty()) {
			return OperateResult.fail(MessageFormat.format(Messages.getString("resDesc.invalidParameter"), "user"));
		}
		if (password == null || password.isEmpty()) {
			return OperateResult.fail(MessageFormat.format(Messages.getString("resDesc.invalidParameter"), "key"));
		}
		int port = ((InetSocketAddress) ch.localAddress()).getPort();
		if (incrementAndGet(username, clientType, port) > joaAlarmConfig.getMaxConnect()) {
			decrementAndGet(username, clientType, port);
            log.warn("Login limit exceeded, User[{}, {}]", username, clientType.getValue());
			return OperateResult.fail(Messages.getString("resDesc.maxLimitExceeded"));
		}
		try {
			KeycloakTokenVo login = keyCloakClientService.loginACKeycloakToken(username, password);
			if (ObjectUtils.isEmpty(login)){
				decrementAndGet(username, clientType, port);
				return OperateResult.fail(Messages.getString("resDesc.loginFail"));
			}
			LoginUser loginUser=LoginUser.processClaimsMap2LoginUser(login.getAccessToken());
			Optional<String> optional = login.getRoles().stream().filter(roleName -> joaAlarmConfig.getRole().startsWith(roleName)).findFirst();
			if (ObjectUtils.isEmpty(loginUser) || optional.isEmpty()) {
				log.warn("user [{}] no permission", username);
				decrementAndGet(username, clientType, port);
				return OperateResult.fail(Messages.getString("resDesc.loginFail"));
			}
			String userKey = UUID.randomUUID().toString();
			ClientSession session = new ClientSession(userKey, getUser(username), password, clientType, port, login);
			ChannelUtil.attachSession(ch, session);
			clients.put(userKey, ch);
			log.info("login successful! current connections {}",clients.size());
		}catch (Exception e) {
			log.error("login fail!", e);
			decrementAndGet(username, clientType, port);
			return OperateResult.fail(Messages.getString("resDesc.loginFail"));
		}
		return OperateResult.SUCCESS;
	}
}
