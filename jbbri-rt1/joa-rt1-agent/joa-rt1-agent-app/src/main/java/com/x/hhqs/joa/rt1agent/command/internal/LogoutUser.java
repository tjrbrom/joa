package com.x.hhqs.joa.rt1agent.command.internal;

import org.springframework.stereotype.Component;

@Component
public class LogoutUser extends Logout {

	@Override
	public String getCommandCode() {
		return "LOGOUT-USER";
	}
}
