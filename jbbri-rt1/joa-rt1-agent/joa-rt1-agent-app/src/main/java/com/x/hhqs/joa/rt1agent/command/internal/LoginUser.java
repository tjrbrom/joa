package com.x.hhqs.joa.rt1agent.command.internal;

import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
public class LoginUser extends Login {

	@Override
	public String getCommandCode() {
		return "LOGIN-USER";
	}

	@Override
	public String getUsername(Properties params) {
		return params.getProperty("USERNAME");
	}

	@Override
	public String getPassword(Properties params) {
		return params.getProperty("PASSWORD");
	}
}
