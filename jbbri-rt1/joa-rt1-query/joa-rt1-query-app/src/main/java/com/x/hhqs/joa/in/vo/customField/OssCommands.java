package com.x.hhqs.joa.in.vo.customField;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "oss-commands")
@XmlAccessorType(XmlAccessType.FIELD)
public class OssCommands {

	@XmlElement(name = "oss-command")
	private List<OssCommand> ossCommands = new ArrayList<OssCommand>();

	public List<OssCommand> getOssCommands() {
		return ossCommands;
	}

	public void setOssCommands(List<OssCommand> ossCommands) {
		this.ossCommands = ossCommands;
	}

	@Override
	public String toString() {
		return "OssCommands [ossCommands=" + ossCommands + "]";
	}

}