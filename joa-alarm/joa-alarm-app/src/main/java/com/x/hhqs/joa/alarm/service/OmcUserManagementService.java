package com.x.hhqs.joa.alarm.service;

//import org.jboss.util.id.ID;


import com.x.hhqs.joa.alarm.model.ClientType;
import com.x.hhqs.joa.alarm.model.OmcUser;
import com.x.hhqs.joa.alarm.model.OperateResult;
import io.netty.channel.Channel;

public interface OmcUserManagementService {

//	boolean validateUser(String username, String password);

	OmcUser getUser(String username);

//	void logout(String sessionId);

	void logout(Channel ch);

	OperateResult login(String username, String password, ClientType clientType, Channel ch);

	//boolean isLoginLimitExceeded(String username, ClientType clientType);
	//boolean isLoginLimitExceeded(String username, ClientType clientType, int port);

}
