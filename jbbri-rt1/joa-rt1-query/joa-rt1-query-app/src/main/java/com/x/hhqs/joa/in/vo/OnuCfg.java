package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.consts.NbiConsts;
import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-ONUCFG")
public class OnuCfg {
	
	@NbiResponseDefinition(fieldName = "AdminState", nbiSpec = NbiConsts.NBI_SPEC_CT)
	private String adminState;

	@NbiResponseDefinition(fieldName = "OperState", nbiSpec = NbiConsts.NBI_SPEC_CT)
	private String operState;

	@NbiResponseDefinition(fieldName = "AUTHTYPE")
	private String authType;

	@NbiResponseDefinition(fieldName = "AUTHINFO")
	private String authInfo;

	@NbiResponseDefinition(fieldName = "Length", nbiSpec = NbiConsts.NBI_SPEC_CT)
	private String length;

	@NbiResponseDefinition(fieldName = "UsFixedBw")
	private String usFixedBw;

	@NbiResponseDefinition(fieldName = "UsAssuredBw")
	private String usAssuredBw;

	@NbiResponseDefinition(fieldName = "UsMaxBw")
	private String usMaxBw;

	@NbiResponseDefinition(fieldName = "DsMaxBw")
	private String dsMaxBw;

	@CustomField(fieldName = "ontTarget")
	@NbiResponseDefinition(fieldName = "", ignored = true)
	private String target;

	public OnuCfg() {
		super();
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

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getAuthInfo() {
		return authInfo;
	}

	public void setAuthInfo(String authInfo) {
		this.authInfo = authInfo;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getUsFixedBw() {
		return usFixedBw;
	}

	public void setUsFixedBw(String usFixedBw) {
		this.usFixedBw = usFixedBw;
	}

	public String getUsAssuredBw() {
		return usAssuredBw;
	}

	public void setUsAssuredBw(String usAssuredBw) {
		this.usAssuredBw = usAssuredBw;
	}

	public String getUsMaxBw() {
		return usMaxBw;
	}

	public void setUsMaxBw(String usMaxBw) {
		this.usMaxBw = usMaxBw;
	}

	public String getDsMaxBw() {
		return dsMaxBw;
	}

	public void setDsMaxBw(String dsMaxBw) {
		this.dsMaxBw = dsMaxBw;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return "OnuCfg [adminState=" + adminState + ", operState=" + operState + ", authType=" + authType
				+ ", authInfo=" + authInfo + ", length=" + length + ", usFixedBw=" + usFixedBw + ", usAssuredBw="
				+ usAssuredBw + ", usMaxBw=" + usMaxBw + ", dsMaxBw=" + dsMaxBw + ", target=" + target + "]";
	}

}
