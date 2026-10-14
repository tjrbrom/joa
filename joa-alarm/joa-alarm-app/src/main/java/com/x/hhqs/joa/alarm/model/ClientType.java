package com.x.hhqs.joa.alarm.model;

public enum ClientType {

	msg("msg"),
	ftp("ftp"),
	unSupport("unSupport");

	private String value = null;

	private ClientType(String value) {
		this.value = value;
	}

	public static ClientType getCientType(String value) {
		for (ClientType cientType : ClientType.values()) {
			if (cientType.value.equalsIgnoreCase(value)) {
				return cientType;
			}
		}

		return unSupport;
	}

	public String getValue() {
		return value;
	}

}
