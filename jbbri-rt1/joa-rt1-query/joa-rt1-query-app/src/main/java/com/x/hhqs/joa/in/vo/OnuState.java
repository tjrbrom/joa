package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.consts.NbiConsts;
import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-ONUSTATE")
public class OnuState implements Comparable<OnuState> {

	@NbiResponseDefinition(fieldName = "ONUID")
	private String onuId;

	@NbiResponseDefinition(fieldName = "AdminState")
	private String adminState;

	@NbiResponseDefinition(fieldName = "OperState")
	private String operState;

	@NbiResponseDefinition(fieldName = "AUTH")
	private String auth;

	@NbiResponseDefinition(fieldName = "AUTHINFO")
	private String authInfo;

	@NbiResponseDefinition(fieldName = "ONUIP")
	private String onuIp;

	@NbiResponseDefinition(fieldName = "LASTOFFTIME")
	private String lastOffTime;

	@NbiResponseDefinition(fieldName = "Length", nbiSpec = NbiConsts.NBI_SPEC_CM)
	private String length;

	@CustomField(fieldName = "ontTarget")
	@NbiResponseDefinition(fieldName = "", ignored = true)
	private String target;

	public OnuState() {
		super();
	}

	public String getOnuId() {
		return onuId;
	}

	public void setOnuId(String onuId) {
		this.onuId = onuId;
	}

	public String getAdminState() {
		return adminState;
	}

	public void setAdminState(String adminState) {
		this.adminState = adminState;
	}

	public String getOperState() {
		return operState;
	}

	public void setOperState(String operState) {
		this.operState = operState;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getAuthInfo() {
		return authInfo;
	}

	public void setAuthInfo(String authInfo) {
		this.authInfo = authInfo;
	}

	public String getOnuIp() {
		return onuIp;
	}

	public void setOnuIp(String onuIp) {
		this.onuIp = onuIp;
	}

	public String getLastOffTime() {
		return lastOffTime;
	}

	public void setLastOffTime(String lastOffTime) {
		this.lastOffTime = lastOffTime;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public int compareTo(OnuState onu) {
		return this.getOnuId().hashCode() - onu.getOnuId().hashCode();
	}

	@Override
	public String toString() {
		return "OnuState [onuId=" + onuId + ", adminState=" + adminState + ", operState=" + operState + ", auth=" + auth
				+ ", authInfo=" + authInfo + ", onuIp=" + onuIp + ", lastOffTime=" + lastOffTime + ", length=" + length
				+ "]";
	}

}
