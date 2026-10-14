package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-ONULANINFO")
public class OnuLanInfo{

	@NbiResponseDefinition(fieldName = "AdminStatus")
	private String adminStatus;
	
	@NbiResponseDefinition(fieldName = "OperStatus")
	private String operStatus;

	@NbiResponseDefinition(fieldName = "DUPLEX")
	private String duplex;
	
	@NbiResponseDefinition(fieldName = "PVID")
	private String pvid;
	
	@NbiResponseDefinition(fieldName = "VLANPRIORITY")
	private String vlanPriprity;

	@NbiResponseDefinition(fieldName = "SPEED")
	private String speed;
	
	@CustomField(fieldName = "ontTarget")
	private String target;
	
	@CustomField(fieldName = "lanId")
	private String lanId;

	public OnuLanInfo() {
		super();
	}

	public String getAdminStatus() {
		return adminStatus;
	}

	public void setAdminStatus(String adminStatus) {
		this.adminStatus = adminStatus;
	}

	public String getOperStatus() {
		return operStatus;
	}

	public void setOperStatus(String operStatus) {
		this.operStatus = operStatus;
	}

	public String getDuplex() {
		return duplex;
	}

	public void setDuplex(String duplex) {
		this.duplex = duplex;
	}

	public String getPvid() {
		return pvid;
	}

	public void setPvid(String pvid) {
		this.pvid = pvid;
	}

	public String getVlanPriprity() {
		return vlanPriprity;
	}

	public void setVlanPriprity(String vlanPriprity) {
		this.vlanPriprity = vlanPriprity;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getLanId() {
		return lanId;
	}

	public void setLanId(String lanId) {
		this.lanId = lanId;
	}

	@Override
	public String toString() {
		return "OnuLanInfo [adminStatus=" + adminStatus + ", operStatus=" + operStatus + ", duplex=" + duplex
				+ ", pvid=" + pvid + ", vlanPriprity=" + vlanPriprity + ", speed=" + speed + "]";
	}

}
