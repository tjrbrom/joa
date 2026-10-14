package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.consts.NbiConsts;
import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-MGCFG")
public class MgCfg implements Comparable<MgCfg> {
	@NbiResponseDefinition(fieldName = "MGID")
	private String mgId;

	@NbiResponseDefinition(fieldName = "PT")
	private String pt;

	@NbiResponseDefinition(fieldName = "EID")
	private String eid;

	@NbiResponseDefinition(fieldName = "SIPREGDM")
	private String sipRegDm;

	@NbiResponseDefinition(fieldName = "SVLAN")
	private String svlan;

	@NbiResponseDefinition(fieldName = "VOIPVLAN")
	private String voipVlan;

	@NbiResponseDefinition(fieldName = "IPMODE")
	private String ipMode;

	@NbiResponseDefinition(fieldName = "IPADDRESS")
	private String ipAddress;

	@NbiResponseDefinition(fieldName = "IPMASK")
	private String ipMask;

	@NbiResponseDefinition(fieldName = "IPGATEWAY")
	private String ipGateWay;

	@NbiResponseDefinition(fieldName = "PPPOEUSER")
	private String pppoeUser;

	@NbiResponseDefinition(fieldName = "PPPOEPWD", nbiSpec = NbiConsts.NBI_SPEC_CT)
	private String PPPOEPWD;

	@NbiResponseDefinition(fieldName = "SCOS")
	private String scos;

	@NbiResponseDefinition(fieldName = "CCOS")
	private String ccos;

	@NbiResponseDefinition(fieldName = "MGCIP1")
	private String mgcIp1;

	@NbiResponseDefinition(fieldName = "MGCIP2")
	private String mgcIp2;

	@NbiResponseDefinition(fieldName = "HEARTBEATMODE")
	private String heartBeatMode;

	@NbiResponseDefinition(fieldName = "HEARTBEATCYCLE")
	private String heartBeatCycle;

	@NbiResponseDefinition(fieldName = "HEARTBEATNUM")
	private String heartBeatNum;

	// l2InfraTarget format is <deviceFxTarget>#<l2Infra>
	@NbiResponseDefinition(fieldName = "", ignored = true)
	@CustomField(fieldName = "l2InfraTarget")
	private String l2InfraTarget;

	@CustomField(fieldName = "sipAgentTarget")
	private String sipAgentTarget;

	@CustomField(fieldName = "sipUserTarget")
	private String sipUserTarget;

	@NbiResponseDefinition(fieldName = "", ignored = true)
	private String serviceProfile;

	@NbiResponseDefinition(fieldName = "", ignored = true)
	private String uni = "";

	public MgCfg() {
		super();
	}

	public String getMgId() {
		return mgId;
	}

	public void setMgId(String mgId) {
		this.mgId = mgId;
	}

	public String getPt() {
		return pt;
	}

	public void setPt(String pt) {
		this.pt = pt;
	}

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public String getSipRegDm() {
		return sipRegDm;
	}

	public void setSipRegDm(String sipRegDm) {
		this.sipRegDm = sipRegDm;
	}

	public String getSvlan() {
		return svlan;
	}

	public void setSvlan(String svlan) {
		this.svlan = svlan;
	}

	public String getVoipVlan() {
		return voipVlan;
	}

	public void setVoipVlan(String voipVlan) {
		this.voipVlan = voipVlan;
	}

	public String getIpMode() {
		return ipMode;
	}

	public void setIpMode(String ipMode) {
		this.ipMode = ipMode;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIpMask() {
		return ipMask;
	}

	public void setIpMask(String ipMask) {
		this.ipMask = ipMask;
	}

	public String getIpGateWay() {
		return ipGateWay;
	}

	public void setIpGateWay(String ipGateWay) {
		this.ipGateWay = ipGateWay;
	}

	public String getPppoeUser() {
		return pppoeUser;
	}

	public void setPppoeUser(String pppoeUser) {
		this.pppoeUser = pppoeUser;
	}

	public String getPPPOEPWD() {
		return PPPOEPWD;
	}

	public void setPPPOEPWD(String pPPOEPWD) {
		PPPOEPWD = pPPOEPWD;
	}

	public String getScos() {
		return scos;
	}

	public void setScos(String scos) {
		this.scos = scos;
	}

	public String getCcos() {
		return ccos;
	}

	public void setCcos(String ccos) {
		this.ccos = ccos;
	}

	public String getMgcIp1() {
		return mgcIp1;
	}

	public void setMgcIp1(String mgcIp1) {
		this.mgcIp1 = mgcIp1;
	}

	public String getMgcIp2() {
		return mgcIp2;
	}

	public void setMgcIp2(String mgcIp2) {
		this.mgcIp2 = mgcIp2;
	}

	public String getHeartBeatMode() {
		return heartBeatMode;
	}

	public void setHeartBeatMode(String heartBeatMode) {
		this.heartBeatMode = heartBeatMode;
	}

	public String getHeartBeatCycle() {
		return heartBeatCycle;
	}

	public void setHeartBeatCycle(String heartBeatCycle) {
		this.heartBeatCycle = heartBeatCycle;
	}

	public String getHeartBeatNum() {
		return heartBeatNum;
	}

	public void setHeartBeatNum(String heartBeatNum) {
		this.heartBeatNum = heartBeatNum;
	}

	public String getL2InfraTarget() {
		return l2InfraTarget;
	}

	public void setL2InfraTarget(String l2InfraTarget) {
		this.l2InfraTarget = l2InfraTarget;
	}

	public String getServiceProfile() {
		return serviceProfile;
	}

	public void setServiceProfile(String serviceProfile) {
		this.serviceProfile = serviceProfile;
	}

	public String getUni() {
		return uni;
	}

	public void setUni(String uni) {
		this.uni = uni;
	}

	public String getSipAgentTarget() {
		return sipAgentTarget;
	}

	public void setSipAgentTarget(String sipAgentTarget) {
		this.sipAgentTarget = sipAgentTarget;
	}

	public String getSipUserTarget() {
		return sipUserTarget;
	}

	public void setSipUserTarget(String sipUserTarget) {
		this.sipUserTarget = sipUserTarget;
	}

	@Override
	public int compareTo(MgCfg o) {
		return this.getUni().hashCode() - o.getUni().hashCode();
	}

	@Override
	public String toString() {
		return "MgCfg [mgId=" + mgId + ", pt=" + pt + ", eId=" + eid + ", sipRegDm=" + sipRegDm + ", sVlan=" + svlan
				+ ", voipVlan=" + voipVlan + ", ipMode=" + ipMode + ", ipAddress=" + ipAddress + ", ipMask=" + ipMask
				+ ", ipGateWay=" + ipGateWay + ", pppoeUser=" + pppoeUser + ", PPPOEPWD=" + PPPOEPWD + ", scos=" + scos
				+ ", ccos=" + ccos + ", mgcIp1=" + mgcIp1 + ", mgcIp2=" + mgcIp2 + ", heartBeatMode=" + heartBeatMode
				+ ", heartBeatCycle=" + heartBeatCycle + ", heartBeatNum=" + heartBeatNum + ", uni=" + uni + "]";
	}

}
