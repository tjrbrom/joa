package com.x.hhqs.joa.alarm.model;


import com.x.hhqs.joa.alarm.vo.KeycloakTokenVo;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ClientSession {

	private String sessionId;

	private OmcUser user;

	private ClientType clientType;

	private int port;

	private String password;

	private KeycloakTokenVo loginUser;

	public ClientSession(String securitySessionId, OmcUser user, String password, ClientType clientType, int port, KeycloakTokenVo loginUser) {
		this.sessionId = securitySessionId;
		this.user = user;
		this.password = password;
		this.clientType = clientType;
		this.port = port;
		this.loginUser = loginUser;
	}

	public String getUserName() {
		return getUser().getName();
	}

}
