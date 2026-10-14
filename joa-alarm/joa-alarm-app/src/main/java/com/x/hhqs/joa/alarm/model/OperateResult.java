package com.x.hhqs.joa.alarm.model;

public class OperateResult {

	public static OperateResult SUCCESS = new OperateResult(true);

	private boolean success = false;

	private String errorMessage;

	private OperateResult(boolean success) {
		this.success = success;
	}

	private OperateResult(boolean success, String errorMessage) {
		this.success = success;
		this.errorMessage = errorMessage;
	}

	public static OperateResult fail(String errorMessage) {
		return new OperateResult(false, errorMessage);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
