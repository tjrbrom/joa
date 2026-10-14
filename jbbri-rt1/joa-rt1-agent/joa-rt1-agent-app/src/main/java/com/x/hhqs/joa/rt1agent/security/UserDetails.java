package com.x.hhqs.joa.rt1agent.security;

import java.io.Serializable;
import java.util.Set;

public interface UserDetails extends Serializable {

    Set<String> getAuthorities();

	String getPassword();

	String getUsername();
	
	Set<String> getPapGroups();
	
	Set<String> getPaps();
}
