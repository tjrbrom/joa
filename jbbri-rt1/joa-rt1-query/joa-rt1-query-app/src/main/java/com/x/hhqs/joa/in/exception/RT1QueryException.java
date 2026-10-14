package com.x.hhqs.joa.in.exception;

import java.util.Arrays;

public class Rt1QueryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String errorCode = "EEEH";

	private String messageKey;

	private String[] messageObjs = new String[] {};

	public Rt1QueryException(String messageKey) {
		super(messageKey);
		this.messageKey = messageKey;
	}

	public Rt1QueryException(String messagekey, String[] messageObjs) {
		super(messagekey);
		this.messageKey = messagekey;
		this.messageObjs = messageObjs;
	}

	public Rt1QueryException(String messagekey, String[] messageObjs, Throwable cause) {
		super(messagekey, cause);
		this.messageKey = messagekey;
		this.messageObjs = messageObjs;
	}

	public Rt1QueryException(String errorCode, String messagekey) {
		super(messagekey);
		this.errorCode = errorCode;
		this.messageKey = messagekey;
	}

	public Rt1QueryException(String errorCode, String messagekey, String[] messageObjs) {
		super(messagekey);
		this.errorCode = errorCode;
		this.messageKey = messagekey;
		this.messageObjs = messageObjs;
	}

	public Rt1QueryException(String errorCode, String messageKey, String[] messageObjs, Throwable cause) {
		super(messageKey, cause);
		this.errorCode = errorCode;
		this.messageKey = messageKey;
		this.messageObjs = messageObjs;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public String[] getMessageObjs() {
		return messageObjs;
	}

	@Override
	public String toString() {
		return "Rt1QueryException [errorCode=" + errorCode + ", messageKey=" + messageKey + ", messageObjs="
				+ Arrays.toString(messageObjs) + "]";
	}

}
