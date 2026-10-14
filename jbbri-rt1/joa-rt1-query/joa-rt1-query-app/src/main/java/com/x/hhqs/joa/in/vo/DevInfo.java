package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.consts.NbiConsts;
import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-DEVINFO")
public class DevInfo {
	@CustomField(fieldName = "deviceTarget")
	private String target;
	
	@NbiResponseDefinition(fieldName = "DEVNAME")
	private String devName;

	@NbiResponseDefinition(fieldName = "DEVIP")
	private String devIp;

	@NbiResponseDefinition(fieldName = "DT")
	private String dt;

	@NbiResponseDefinition(fieldName = "DEVER")
	private String deVer;

	@NbiResponseDefinition(fieldName = "MEM")
	private String mem;

	@NbiResponseDefinition(fieldName = "CPU")
	private String cpu;

	@NbiResponseDefinition(fieldName = "TEMPERATURE")
	private String temperature;
	
	@NbiResponseDefinition(fieldName = "TOPOLOC", nbiSpec = NbiConsts.NBI_SPEC_CM)
	private String topoloc;

	public DevInfo() {
		super();
	}
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public String getDevIp() {
		return devIp;
	}

	public void setDevIp(String devIp) {
		this.devIp = devIp;
	}

	public String getDt() {
		return dt;
	}

	public void setDt(String dt) {
		this.dt = dt;
	}

	public String getDeVer() {
		return deVer;
	}

	public void setDeVer(String deVer) {
		this.deVer = deVer;
	}

	public String getMem() {
		return mem;
	}

	public void setMem(String mem) {
		this.mem = mem;
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getTopoloc() {
		return topoloc;
	}

	public void setTopoloc(String topoloc) {
		this.topoloc = topoloc;
	}

	@Override
	public String toString() {
		return "DevInfo [devName=" + devName + ", devIp=" + devIp + ", dt=" + dt + ", deVer=" + deVer + ", mem=" + mem
				+ ", cpu=" + cpu + ", temperature=" + temperature + ", topoloc=" + topoloc + "]";
	}

}
