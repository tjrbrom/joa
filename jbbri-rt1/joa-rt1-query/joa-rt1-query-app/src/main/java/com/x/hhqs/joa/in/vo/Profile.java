package com.x.hhqs.joa.in.vo;

import java.util.HashMap;
import java.util.Map;

public class Profile {

	private String name;

	private String type;

	private String subType;

	private String version;

	private String baseRelease;

	private Map<String, String> specificData = new HashMap<String, String>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getBaseRelease() {
		return baseRelease;
	}

	public void setBaseRelease(String baseRelease) {
		this.baseRelease = baseRelease;
	}

	public Map<String, String> getSpecificData() {
		return specificData;
	}

	public void setSpecificData(Map<String, String> specificData) {
		this.specificData = specificData;
	}

	@Override
	public String toString() {
		return "Profile [name=" + name + ", type=" + type + ", subType=" + subType + ", version=" + version
				+ ", baseRelease=" + baseRelease + ", specificData=" + specificData + "]";
	}

}
