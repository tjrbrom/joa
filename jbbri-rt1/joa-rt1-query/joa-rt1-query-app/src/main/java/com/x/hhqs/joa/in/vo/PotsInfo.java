package com.x.hhqs.joa.in.vo;

public class PotsInfo {

	@NbiResponseDefinition(fieldName = "LineState")
	private String lineState;

	@NbiResponseDefinition(fieldName = "ServiceState")
	private String serviceState;

	@NbiResponseDefinition(fieldName = "EchoCancel")
	private String echoCancel;

	@NbiResponseDefinition(fieldName = "ReversedPolarity")
	private String reversedPolarity;

	@NbiResponseDefinition(fieldName = "RxGain")
	private String rxGain;

	@NbiResponseDefinition(fieldName = "TxGain")
	private String txGain;

	@NbiResponseDefinition(fieldName = "PN")
	private String pn;

	@NbiResponseDefinition(fieldName = "TID")
	private String tid;

	@NbiResponseDefinition(fieldName = "SIPUSERNAME")
	private String sipUserName;

	@NbiResponseDefinition(fieldName = "SIPUSERPWD")
	private String sipUserPwd;

	@NbiResponseDefinition(fieldName = "MGID")
	private String mgId;

	// POTS_TEL1
	@NbiResponseDefinition(fieldName = "", ignored = true)
	private String voipUniId;
	
	public PotsInfo() {
		super();
	}

	public String getLineState() {
		return lineState;
	}

	public void setLineState(String lineState) {
		this.lineState = lineState;
	}

	public String getServiceState() {
		return serviceState;
	}

	public void setServiceState(String serviceState) {
		this.serviceState = serviceState;
	}

	public String getEchoCancel() {
		return echoCancel;
	}

	public void setEchoCancel(String echoCancel) {
		this.echoCancel = echoCancel;
	}

	public String getReversedPolarity() {
		return reversedPolarity;
	}

	public void setReversedPolarity(String reversedPolarity) {
		this.reversedPolarity = reversedPolarity;
	}

	public String getRxGain() {
		return rxGain;
	}

	public void setRxGain(String rxGain) {
		this.rxGain = rxGain;
	}

	public String getTxGain() {
		return txGain;
	}

	public void setTxGain(String txGain) {
		this.txGain = txGain;
	}

	public String getPn() {
		return pn;
	}

	public void setPn(String pn) {
		this.pn = pn;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getSipUserName() {
		return sipUserName;
	}

	public void setSipUserName(String sipUserName) {
		this.sipUserName = sipUserName;
	}

	public String getSipUserPwd() {
		return sipUserPwd;
	}

	public void setSipUserPwd(String sipUserPwd) {
		this.sipUserPwd = sipUserPwd;
	}

	public String getMgId() {
		return mgId;
	}

	public void setMgId(String mgId) {
		this.mgId = mgId;
	}

	public String getVoipUniId() {
		return voipUniId;
	}

	public void setVoipUniId(String voipUniId) {
		this.voipUniId = voipUniId;
	}
	
	@Override
	public String toString() {
		return "PotsInfo [lineState=" + lineState + ", serviceState=" + serviceState + ", echoCancel=" + echoCancel
				+ ", reversedPolarity=" + reversedPolarity + ", rxGain=" + rxGain + ", txGain=" + txGain + ", pn=" + pn
				+ ", tid=" + tid + ", sipUserName=" + sipUserName + ", sipUserPwd=" + sipUserPwd + ", mgId=" + mgId
				+ "]";
	}

}
