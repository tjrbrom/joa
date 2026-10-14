package com.x.hhqs.joa.in.vo.customField;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Attribute {
	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "xPath")
	private String xPath;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getXPath() {
		return xPath;
	}

	public void setXPath(String xPath) {
		this.xPath = xPath;
	}

	@Override
	public String toString() {
		return "Attribute [name=" + name + ", xPath=" + xPath + "]";
	}

}