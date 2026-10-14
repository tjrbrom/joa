//package com.x.hhqs.joa.alarm.netty.alarm.logger;
//
//import java.io.Serializable;
//import java.util.Date;
//
//public class OasAlarmInfo implements Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	private Long id;
//
//	private Long alarmSeq;
//
//	private Date createTime;
//
//	private String jsonValue;
//
//	private Integer channelIndex;
//
//	private String alarmId;
//
//	private String neName;
//
//	public static OasAlarmInfo create(Long id, Long alarmSeq, Date createTime, String jsonValue, Integer channelIndex, String alarmId, String neName) {
//		return new OasAlarmInfo(id, alarmSeq, createTime, jsonValue, channelIndex, alarmId, neName);
//	}
//
//	public OasAlarmInfo() {
//	}
//
//	private OasAlarmInfo(Long id, Long alarmSeq, Date createTime, String jsonValue, Integer channelIndex, String alarmId, String neName) {
//		super();
//		this.id = id;
//		this.alarmSeq = alarmSeq;
//		this.createTime = createTime;
//		this.jsonValue = jsonValue;
//		this.channelIndex = channelIndex;
//		this.alarmId = alarmId;
//		this.neName = neName;
//	}
//
//	private OasAlarmInfo(Long id, Long alarmSeq, Date createTime, String jsonValue) {
//		this.id = id;
//		this.alarmSeq = alarmSeq;
//		this.createTime = createTime;
//		this.jsonValue = jsonValue;
//	}
//
//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}
//
//	public Long getAlarmSeq() {
//		return alarmSeq;
//	}
//
//	public void setAlarmSeq(Long alarmSeq) {
//		this.alarmSeq = alarmSeq;
//	}
//
//	public String getJsonValue() {
//		return jsonValue;
//	}
//
//	public void setJsonValue(String jsonValue) {
//		this.jsonValue = jsonValue;
//	}
//
//	public Date getCreateTime() {
//		return createTime;
//	}
//
//	public void setCreateTime(Date createTime) {
//		this.createTime = createTime;
//	}
//
//	@Override
//	public String toString() {
//		return "OasAlarmInfo [id=" + id + ", alarmSeq=" + alarmSeq + ", createTime=" + createTime + ", jsonValue="
//				+ jsonValue + "]";
//	}
//
//	public Integer getChannelIndex() {
//		return channelIndex;
//	}
//
//	public void setChannelIndex(Integer channelIndex) {
//		this.channelIndex = channelIndex;
//	}
//
//	public String getAlarmId() {
//		return alarmId;
//	}
//
//	public void setAlarmId(String alarmId) {
//		this.alarmId = alarmId;
//	}
//
//	public String getNeName() {
//		return neName;
//	}
//
//	public void setNeName(String neName) {
//		this.neName = neName;
//	}
//}
