package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-PONINFO")
public class PonInfo implements Comparable<PonInfo> {
	@NbiResponseDefinition(fieldName = "AdminState")
	private String adminState;

	@NbiResponseDefinition(fieldName = "OperState")
	private String operState;
	
	@CustomField(fieldName = "fiberTarget")
	private String target;

	public PonInfo() {
		super();
	}

	public PonInfo(String adminState, String operState) {
		super();
		this.adminState = adminState;
		this.operState = operState;
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
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public int compareTo(PonInfo o) {
		return 0;
	}

	@Override
	public String toString() {
		return "PonInfo [adminState=" + adminState + ", operState=" + operState + "]";
	}

}
