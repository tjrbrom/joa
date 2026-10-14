package com.x.hhqs.joa.rt1agent.command;


public class EmptyResponse {
	public static final EmptyResponse Instance = new EmptyResponse();

	private EmptyResponse() {
	}

	@Override
	public final String toString() {
		return "";
	}
}
