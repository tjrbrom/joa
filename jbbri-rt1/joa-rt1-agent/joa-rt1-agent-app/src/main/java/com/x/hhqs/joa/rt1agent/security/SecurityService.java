package com.x.hhqs.joa.rt1agent.security;

public interface SecurityService {

	Authentication login(String username, String password);

	void logout(Authentication authentication);
	
	boolean hasAuthorization(Authentication authentication, String commandCode);
	
	PermissionCheckResult checkSecondaryAuthPermission(String username, String password) throws Exception;

}
