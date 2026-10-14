package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-POS")
public class Pos implements Comparable<Pos> {

	@CustomField(fieldName = "splitterTarget")
	private String target;

	@NbiResponseDefinition(fieldName = "NAME")
	private String name;

	@NbiResponseDefinition(fieldName = "POSID")
	private String posId;

	@NbiResponseDefinition(fieldName = "RATION")
	private String ration;

	@NbiResponseDefinition(fieldName = "LAYER")
	private String layer;

	@NbiResponseDefinition(fieldName = "TOPPOSID")
	private String topPosId;

	@NbiResponseDefinition(fieldName = "TOPPOSPORT")
	private String topPosPort;

	public Pos() {
		super();
	}

	public Pos(String target, String name, String posId, String ration, String layer, String topPosId,
			String topPosPort) {
		super();
		this.target = target;
		this.name = name;
		this.posId = posId;
		this.ration = ration;
		this.layer = layer;
		this.topPosId = topPosId;
		this.topPosPort = topPosPort;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPosId() {
		return posId;
	}

	public void setPosId(String posId) {
		this.posId = posId;
	}

	public String getRation() {
		return ration;
	}

	public void setRation(String ration) {
		this.ration = ration;
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}

	public String getTopPosId() {
		return topPosId;
	}

	public void setTopPosId(String topPosId) {
		this.topPosId = topPosId;
	}

	public String getTopPosPort() {
		return topPosPort;
	}

	public void setTopPosPort(String topPosPort) {
		this.topPosPort = topPosPort;
	}

	@Override
	public int compareTo(Pos pos) {
	    int cmp = getPonId(this).compareTo(getPonId(pos));
	    if (cmp != 0) {
	        return cmp;
	    }
	    return getPosPort(this).compareTo(getPosPort(pos));
	}
	
	private String getPonId(Pos pos) {
		String[] parts = pos.getPosId().split("-");
		return parts[0] + "-" + parts[1] + "-" + parts[2] + "-" + parts[3];
	}

	private String getPosPort(Pos pos) {
		return pos.getPosId().split("-")[4];
	}

	@Override
	public String toString() {
		return "Pos [target=" + target + ", name=" + name + ", posId=" + posId + ", ration=" + ration + ", layer="
				+ layer + ", topPosId=" + topPosId + ", topPosPort=" + topPosPort + "]";
	}

}
