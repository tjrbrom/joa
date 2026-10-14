package com.x.hhqs.joa.alarm.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
public class OmcAlarmVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	private Long alarmSeq;

	private Timestamp createTime;

	private String jsonValue;

	private Integer channelIndex;
	
	private String alarmId;

	private String neName;

	public OmcAlarmVO() {
	}


	public static OmcAlarmVO create(Long id, Long alarmSeq, Timestamp createTime, String jsonValue, Integer channelIndex, String alarmId, String neName) {
		return new OmcAlarmVO(id, alarmSeq, createTime, jsonValue, channelIndex, alarmId, neName);
	}

	private OmcAlarmVO(Long id, Long alarmSeq, Timestamp createTime, String jsonValue, Integer channelIndex, String alarmId, String neName) {
		super();
		this.id = id;
		this.alarmSeq = alarmSeq;
		this.createTime = createTime;
		this.jsonValue = jsonValue;
		this.channelIndex = channelIndex;
		this.alarmId = alarmId;
		this.neName = neName;
	}

	private OmcAlarmVO(Long id, Long alarmSeq, Timestamp createTime, String jsonValue) {
		this.id = id;
		this.alarmSeq = alarmSeq;
		this.createTime = createTime;
		this.jsonValue = jsonValue;
	}


	@Override
	public String toString() {
		return "OasAlarmInfo [id=" + id + ", alarmSeq=" + alarmSeq + ", createTime=" + createTime + ", jsonValue="
				+ jsonValue + "]";
	}

}
