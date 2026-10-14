package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-VLAN")
public class Vlan implements Comparable<Vlan> {
	
	@CustomField(fieldName = "l2InfraTarget")
	private String target;

	@NbiResponseDefinition(fieldName = "ONUIP")
	private String onuIp;

	@NbiResponseDefinition(fieldName = "OLTID")
	private String oltId;

	@NbiResponseDefinition(fieldName = "VLAN")
	private String vlan;

	@NbiResponseDefinition(fieldName = "DESC")
	private String desc;

	@NbiResponseDefinition(fieldName = "VLANMODE")
	private String vlanMode;

	@NbiResponseDefinition(fieldName = "PORTLIST")
	private String portList;

	@NbiResponseDefinition(fieldName = "MVLANFLAG")
	private String mvlanFlag;

	@NbiResponseDefinition(fieldName = "MVLANPRI")
	private String mvlanPri;

	@NbiResponseDefinition(fieldName = "SERVICE")
	private String service;

	public Vlan() {
		super();
	}
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getOnuIp() {
		return onuIp;
	}

	public void setOnuIp(String onuIp) {
		this.onuIp = onuIp;
	}

	public String getOltId() {
		return oltId;
	}

	public void setOltId(String oltId) {
		this.oltId = oltId;
	}

	public String getVlan() {
		return vlan;
	}

	public void setVlan(String vlan) {
		this.vlan = vlan;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getVlanMode() {
		return vlanMode;
	}

	public void setVlanMode(String vlanMode) {
		this.vlanMode = vlanMode;
	}

	public String getPortList() {
		return portList;
	}

	public void setPortList(String portList) {
		this.portList = portList;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getMvlanFlag() {
		return mvlanFlag;
	}

	public void setMvlanFlag(String mvlanFlag) {
		this.mvlanFlag = mvlanFlag;
	}

	public String getMvlanPri() {
		return mvlanPri;
	}

	public void setMvlanPri(String mvlanPri) {
		this.mvlanPri = mvlanPri;
	}

	@Override
	public int compareTo(Vlan o) {
		return this.getVlan().hashCode() - o.getVlan().hashCode();
	}

	@Override
	public String toString() {
		return "Vlan [onuIp=" + onuIp + ", oltId=" + oltId + ", vlan=" + vlan + ", desc=" + desc + ", vlanMode="
				+ vlanMode + ", portList=" + portList + ", mvlanFlag=" + mvlanFlag + ", mvlanPri=" + mvlanPri
				+ ", service=" + service + "]";
	}

}
