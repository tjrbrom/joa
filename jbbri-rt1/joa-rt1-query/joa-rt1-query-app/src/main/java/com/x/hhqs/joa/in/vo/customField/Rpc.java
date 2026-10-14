package com.x.hhqs.joa.in.vo.customField;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Rpc {
	@XmlAttribute(name = "file")
	private String file;

	@XmlAttribute(name = "type")
	private String type;

	@XmlAttribute(name = "targetName")
	private String targetName;

	@XmlElement(name = "attribute")
	private List<Attribute> attributes = new ArrayList<Attribute>();

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return "Rpc [file=" + file + ", type=" + type + ", targetName=" + targetName + ", attributes=" + attributes
				+ "]";
	}

}