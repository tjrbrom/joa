package com.x.hhqs.joa.in.vo;

public class TopoLink implements Comparable<TopoLink> {

	@NbiResponseDefinition(fieldName = "PONID")
	private String ponId;

	@NbiResponseDefinition(fieldName = "NAME")
	private String name;

	@NbiResponseDefinition(fieldName = "APOINT")
	private String apoint;

	@NbiResponseDefinition(fieldName = "ZPOINT")
	private String zpoint;

	public TopoLink() {
		super();
	}

	public TopoLink(String ponId, String name, String apoint, String zpoint) {
		super();
		this.ponId = ponId;
		this.name = name;
		this.apoint = apoint;
		this.zpoint = zpoint;
	}

	public String getPonId() {
		return ponId;
	}

	public void setPonId(String ponId) {
		this.ponId = ponId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApoint() {
		return apoint;
	}

	public void setApoint(String apoint) {
		this.apoint = apoint;
	}

	public String getZpoint() {
		return zpoint;
	}

	public void setZpoint(String zpoint) {
		this.zpoint = zpoint;
	}

	@Override
	public int compareTo(TopoLink o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		return "TopoLink [ponId=" + ponId + ", name=" + name + ", apoint=" + apoint + ", zpoint=" + zpoint + "]";
	}

}
