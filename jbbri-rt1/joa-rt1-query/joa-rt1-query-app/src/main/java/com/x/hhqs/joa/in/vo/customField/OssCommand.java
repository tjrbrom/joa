package com.x.hhqs.joa.in.vo.customField;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class OssCommand {
	@XmlAttribute(name = "name")
	private String name;

	@XmlElement(name = "rpc")
	private List<Rpc> rpc = new ArrayList<Rpc>();

	@XmlElementWrapper(name = "ignore")
	@XmlElement(name = "attribute")
	private List<Attribute> ignoreAttributes = new ArrayList<Attribute>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Rpc> getRpc() {
		return rpc;
	}

	public void setRpc(List<Rpc> rpc) {
		this.rpc = rpc;
	}

	public List<Attribute> getIgnoreAttributes() {
		return ignoreAttributes;
	}

	public void setIgnoreAttributes(List<Attribute> ignoreAttributes) {
		this.ignoreAttributes = ignoreAttributes;
	}

	@Override
	public String toString() {
		return "OssCommand [name=" + name + ", rpc=" + rpc + ", ignoreAttributes=" + ignoreAttributes + "]";
	}

}