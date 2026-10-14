package com.x.hhqs.joa.rt1agent.security.dev;

import com.x.hhqs.joa.rt1agent.security.Authentication;
import com.x.hhqs.joa.rt1agent.security.User;

public class DevAuthentication implements Authentication {

    private User user;

	private boolean valid = false;
	
	public DevAuthentication(User user, boolean valid) {
		super();
		this.user = user;
		this.valid = valid;
	}

	@Override
	public String getName() {
		return user.getUsername();
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public User getUserDetails() {
		return user;
	}

}
