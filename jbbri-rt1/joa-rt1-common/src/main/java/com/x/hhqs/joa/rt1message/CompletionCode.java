package com.x.hhqs.joa.rt1message;

/*-
 * completionCode:
 * COMPLD: 命令执行正确
 * DELAY：命令被延迟执行
 * DENY：命令执行失败
 * PRTL：命令被部分执行
 * RTRV：返回命令中已测试的测试结果，其他项目正在测试
 */
public enum CompletionCode {
	COMPLD("COMPLD"), DELAY("DELAY"), DENY("DENY"), PRTL("PRTL"), RTRV("RTRV");

	private final String code;

	CompletionCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public String toString() {
		return this.code;
	}
}
