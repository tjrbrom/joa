package com.x.hhqs.joa.alarm.listener;

import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;

import java.util.List;

public interface InnerAlarmListener {

	void alarmsReceived(List<OmcAlarmVO> alarms);

}
