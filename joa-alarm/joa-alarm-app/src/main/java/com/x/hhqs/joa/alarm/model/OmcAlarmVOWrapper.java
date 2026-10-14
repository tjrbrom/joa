package com.x.hhqs.joa.alarm.model;

import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class OmcAlarmVOWrapper implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	enum AlarmWrapperType {
		alarm,
		alarmList,
		syncAlarmList;
	}

	private AlarmWrapperType type;

	private OmcAlarmVO omcAlarmVO;

	private List<OmcAlarmVO> omcAlarmVOs;

	private OmcAlarmVOWrapper() {
	}

	public OmcAlarmVOWrapper(OmcAlarmVO omcAlarmVO) {
		this.omcAlarmVO = omcAlarmVO;
		this.type = AlarmWrapperType.alarm;
	}

	public OmcAlarmVOWrapper(List<OmcAlarmVO> alarms) {
		this.omcAlarmVOs = alarms;
		this.type = AlarmWrapperType.alarmList;
	}

	public OmcAlarmVOWrapper resetAlarms(List<OmcAlarmVO> omcAlarmVOs) {
		OmcAlarmVOWrapper wrapper = new OmcAlarmVOWrapper();
		wrapper.omcAlarmVOs = omcAlarmVOs;
		wrapper.type = this.type;

		return wrapper;
	}

	public static OmcAlarmVOWrapper wrapperSyncAlarms(List<OmcAlarmVO> omcAlarmVOs) {
		OmcAlarmVOWrapper wrapper = new OmcAlarmVOWrapper();
		wrapper.omcAlarmVOs = omcAlarmVOs;
		wrapper.type = AlarmWrapperType.syncAlarmList;

		return wrapper;
	}

	public boolean isAlarmList() {
		return type == AlarmWrapperType.alarmList;
	}

	public boolean isSyncAlarms() {
		return type == AlarmWrapperType.syncAlarmList;
	}

}
