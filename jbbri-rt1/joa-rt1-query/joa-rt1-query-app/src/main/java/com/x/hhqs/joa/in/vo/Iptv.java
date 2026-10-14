package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-IPTV")
public class Iptv implements Comparable<Iptv> {
	@NbiResponseDefinition(fieldName = "ONUIP")
	private String onuIp;

	@NbiResponseDefinition(fieldName = "OLTID")
	private String oltId;

	@NbiResponseDefinition(fieldName = "PONID")
	private String ponId;

	@NbiResponseDefinition(fieldName = "ONUID")
	private String onuId;

	@NbiResponseDefinition(fieldName = "ONUPORT")
	private String onuPort;

	@NbiResponseDefinition(fieldName = "MVLAN")
	private String mvlan;

	@NbiResponseDefinition(fieldName = "VPI")
	private String vpi;

	@NbiResponseDefinition(fieldName = "VCI")
	private String vci;

	@NbiResponseDefinition(fieldName = "UV")
	private String uv;

	@NbiResponseDefinition(fieldName = "FLMODE")
	private String flMode;

	@NbiResponseDefinition(fieldName = "MAXGRP")
	private String maxGrp;

	@CustomField(fieldName = "l2UserTarget")
	private String l2UserTarget;
	
	// l2InfraTarget format is <deviceFxTarget>#<l2Infra>
	@NbiResponseDefinition(fieldName = "", ignored = true)
	@CustomField(fieldName = "l2InfraTarget")
	private String l2InfraTarget;

	// mcastUserTarget format is <serviceName>#<customerName>
	@NbiResponseDefinition(fieldName = "", ignored = true)
	@CustomField(fieldName = "mcastUserTarget")
	private String mcastUserTarget;

	@NbiResponseDefinition(fieldName = "", ignored = true)
	private String mcastServiceProfile;
	

	public Iptv() {
		super();
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

	public String getPonId() {
		return ponId;
	}

	public void setPonId(String ponId) {
		this.ponId = ponId;
	}

	public String getOnuId() {
		return onuId;
	}

	public void setOnuId(String onuId) {
		this.onuId = onuId;
	}

	public String getOnuPort() {
		return onuPort;
	}

	public void setOnuPort(String onuPort) {
		this.onuPort = onuPort;
	}

	public String getMvlan() {
		return mvlan;
	}

	public void setMvlan(String mvlan) {
		this.mvlan = mvlan;
	}

	public String getVpi() {
		return vpi;
	}

	public void setVpi(String vpi) {
		this.vpi = vpi;
	}

	public String getVci() {
		return vci;
	}

	public void setVci(String vci) {
		this.vci = vci;
	}

	public String getUv() {
		return uv;
	}

	public void setUv(String uv) {
		this.uv = uv;
	}

	public String getFlMode() {
		return flMode;
	}

	public void setFlMode(String flMode) {
		this.flMode = flMode;
	}

	public String getMaxGrp() {
		return maxGrp;
	}

	public void setMaxGrp(String maxGrp) {
		this.maxGrp = maxGrp;
	}

	public String getL2UserTarget() {
		return l2UserTarget;
	}

	public void setL2UserTarget(String l2UserTarget) {
		this.l2UserTarget = l2UserTarget;
	}

	public String getL2InfraTarget() {
		return l2InfraTarget;
	}

	public void setL2InfraTarget(String l2InfraTarget) {
		this.l2InfraTarget = l2InfraTarget;
	}

	public String getMcastUserTarget() {
		return mcastUserTarget;
	}

	public void setMcastUserTarget(String mcastUserTarget) {
		this.mcastUserTarget = mcastUserTarget;
	}

	public String getMcastServiceProfile() {
		return mcastServiceProfile;
	}

	public void setMcastServiceProfile(String mcastServiceProfile) {
		this.mcastServiceProfile = mcastServiceProfile;
	}

	@Override
	public int compareTo(Iptv o) {
		return this.getOnuPort().hashCode() - o.getOnuPort().hashCode();
	}

	@Override
	public String toString() {
		return "IPTV [onuIp=" + onuIp + ", oltId=" + oltId + ", ponId=" + ponId + ", onuId=" + onuId + ", onuPort="
				+ onuPort + ", mvlan=" + mvlan + ", vpi=" + vpi + ", vci=" + vci + ", uv=" + uv + ", flMode=" + flMode
				+ ", maxGrp=" + maxGrp + "]";
	}

}
