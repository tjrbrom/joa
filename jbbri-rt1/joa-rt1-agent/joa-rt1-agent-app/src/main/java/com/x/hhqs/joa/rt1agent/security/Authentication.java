package com.x.hhqs.joa.rt1agent.security;

public interface Authentication {

	String getName();

	UserDetails getUserDetails();

	boolean isValid();

	default String getErrorMessage() {
		return null;
	}
}
