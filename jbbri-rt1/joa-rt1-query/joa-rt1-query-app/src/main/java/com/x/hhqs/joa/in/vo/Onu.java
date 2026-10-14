package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-ONU")
public class Onu implements Comparable<Onu> {
	
	@CustomField(fieldName = "ontTarget")
	private String target;

	@NbiResponseDefinition(fieldName = "OLTID")
	private String oltId;

	@NbiResponseDefinition(fieldName = "PONID")
	private String ponId;

	@NbiResponseDefinition(fieldName = "ONUNO")
	private String onuNo;

	@NbiResponseDefinition(fieldName = "NAME")
	private String name;

	@NbiResponseDefinition(fieldName = "DESC")
	private String desc;

	@NbiResponseDefinition(fieldName = "ONUTYPE")
	private String onuType;

	@NbiResponseDefinition(fieldName = "IP")
	private String ip;

	@NbiResponseDefinition(fieldName = "AUTHTYPE")
	private String auth;

	@NbiResponseDefinition(fieldName = "MAC")
	private String mac;

	@NbiResponseDefinition(fieldName = "LOID")
	private String loid;

	@NbiResponseDefinition(fieldName = "PWD")
	private String pwd;

	@NbiResponseDefinition(fieldName = "SWVER")
	private String swver;

	@NbiResponseDefinition(fieldName = "FTTXMODE")
	private String fttxMode;

	public Onu() {
		super();
	}
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
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

	public String getOnuNo() {
		return onuNo;
	}

	public void setOnuNo(String onuNo) {
		this.onuNo = onuNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getOnuType() {
		return onuType;
	}

	public void setOnuType(String onuType) {
		this.onuType = onuType;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getLoid() {
		return loid;
	}

	public void setLoid(String loid) {
		this.loid = loid;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getSwver() {
		return swver;
	}

	public void setSwver(String swver) {
		this.swver = swver;
	}

	public String getFttxMode() {
		return fttxMode;
	}

	public void setFttxMode(String fttxMode) {
		this.fttxMode = fttxMode;
	}

	@Override
	public int compareTo(Onu onu) {
		int rst = 0;
		int tmp = this.getPonId().hashCode() - onu.getPonId().hashCode();
		if (tmp != 0) {
			rst = (tmp > 0) ? 2 : -1;
		} else {
			tmp = this.getOnuNo().hashCode() - onu.getOnuNo().hashCode();
			rst = (tmp > 0) ? 1 : -2;
		}
		return rst;
	}

	@Override
	public String toString() {
		return "Onu [oltId=" + oltId + ", ponId=" + ponId + ", onuNo=" + onuNo + ", name=" + name + ", desc=" + desc
				+ ", onuType=" + onuType + ", ip=" + ip + ", auth=" + auth + ", mac=" + mac + ", loid=" + loid
				+ ", pwd=" + pwd + ", swver=" + swver + ", fttxMode=" + fttxMode + "]";
	}

}
