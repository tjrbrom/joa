package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.vo.customField.CustomField;

/**
 * @author lli021
 *
 */
@CustomField(ossName = "LST-LANPORT")
public class LanPort implements Comparable<LanPort> {

	@NbiResponseDefinition(fieldName = "ONUIP")
	private String onuIp;

	@NbiResponseDefinition(fieldName = "OLTID")
	private String oltId;

	@NbiResponseDefinition(fieldName = "PONID")
	private String ponId;

	@NbiResponseDefinition(fieldName = "ONUID")
	private String onuId;

	@NbiResponseDefinition(fieldName = "PORTID")
	private String portId;

	@NbiResponseDefinition(fieldName = "ADMINSTATUS")
	private String adminStatus;

	@NbiResponseDefinition(fieldName = "DUPLEX")
	private String duplex;

	@NbiResponseDefinition(fieldName = "SPEED")
	private String speed;

	@NbiResponseDefinition(fieldName = "RateLimitUs")
	private String rateLimitUs;

	@NbiResponseDefinition(fieldName = "RateLimitDs")
	private String rateLimitDs;

	// ENET_LAN1
	@NbiResponseDefinition(fieldName = "", ignored = true)
	private String uniId;
	
	// VENET_ALCLF8AFFD92_LAN1
    @NbiResponseDefinition(fieldName = "", ignored = true)
	 private String venetId;
	
	@NbiResponseDefinition(fieldName = "", ignored = true)
	private String ntId;
	
	@CustomField(fieldName = "ontTarget")
	private String target;
	
	// LAN1
	@CustomField(fieldName = "lanId")
	private String lanId;


	public LanPort() {
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

	public String getPortId() {
		return portId;
	}

	public void setPortId(String portId) {
		this.portId = portId;
	}

	public String getAdminStatus() {
		return adminStatus;
	}

	public void setAdminStatus(String adminStatus) {
		this.adminStatus = adminStatus;
	}

	public String getDuplex() {
		return duplex;
	}

	public void setDuplex(String duplex) {
		this.duplex = duplex;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getRateLimitUs() {
		return rateLimitUs;
	}

	public void setRateLimitUs(String rateLimitUs) {
		this.rateLimitUs = rateLimitUs;
	}

	public String getRateLimitDs() {
		return rateLimitDs;
	}

	public void setRateLimitDs(String rateLimitDs) {
		this.rateLimitDs = rateLimitDs;
	}

	public String getUniId() {
		return uniId;
	}

	public void setUniId(String uniId) {
		this.uniId = uniId;
	}
	
	public String getVenetId() {
		return venetId;
	}

	public void setVenetId(String venetId) {
		this.venetId = venetId;
	}

	public String getNtId() {
		return ntId;
	}

	public void setNtId(String ntId) {
		this.ntId = ntId;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getLanId() {
		return lanId;
	}

	public void setLanId(String lanId) {
		this.lanId = lanId;
	}

	@Override
	public int compareTo(LanPort lanPort) {
		return this.getPortId().hashCode() - lanPort.getPortId().hashCode();
	}

	@Override
	public String toString() {
		return "LanPort [onuIp=" + onuIp + ", oltId=" + oltId + ", ponId=" + ponId + ", onuId=" + onuId + ", portId="
				+ portId + ", adminStatus=" + adminStatus + ", duplex=" + duplex + ", speed=" + speed + ", rateLimitUs="
				+ rateLimitUs + ", rateLimitDs=" + rateLimitDs + "]";
	}

}
