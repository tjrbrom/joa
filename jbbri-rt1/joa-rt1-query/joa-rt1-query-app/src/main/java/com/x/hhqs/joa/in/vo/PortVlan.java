package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-PORTVLAN")
public class PortVlan implements Comparable<PortVlan> {
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

	@NbiResponseDefinition(fieldName = "SVLAN")
	private String svlan;

	@NbiResponseDefinition(fieldName = "CVLAN")
	private String cvlan;

	@NbiResponseDefinition(fieldName = "VPI")
	private String vpi;

	@NbiResponseDefinition(fieldName = "VCI")
	private String vci;

	@NbiResponseDefinition(fieldName = "UV")
	private String uv;

	// l2InfraTarget format is <deviceFxTarget>#<l2Infra>
	@NbiResponseDefinition(fieldName = "", ignored = true)
	@CustomField(fieldName = "l2InfraTarget")
	private String l2Infra;
	
	@CustomField(fieldName = "l2UserTarget")
	private String l2User;

	public PortVlan() {
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

	public String getSvlan() {
		return svlan;
	}

	public void setSvlan(String svlan) {
		this.svlan = svlan;
	}

	public String getCvlan() {
		return cvlan;
	}

	public void setCvlan(String cvlan) {
		this.cvlan = cvlan;
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

	public String getL2Infra() {
		return l2Infra;
	}

	public void setL2Infra(String l2Infra) {
		this.l2Infra = l2Infra;
	}
	
	public String getL2User() {
		return l2User;
	}

	public void setL2User(String l2User) {
		this.l2User = l2User;
	}

	@Override
	public int compareTo(PortVlan o) {
		return this.getOnuPort().hashCode() - o.getOnuPort().hashCode();
	}

	@Override
	public String toString() {
		return "PortVlan [onuIp=" + onuIp + ", oltId=" + oltId + ", ponId=" + ponId + ", onuId=" + onuId + ", onuPort="
				+ onuPort + ", svlan=" + svlan + ", cvlan=" + cvlan + ", vpi=" + vpi + ", vci=" + vci + ", uv=" + uv
				+ "]";
	}

}
