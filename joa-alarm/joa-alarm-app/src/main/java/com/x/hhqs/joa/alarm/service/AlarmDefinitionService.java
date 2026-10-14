package com.x.hhqs.joa.alarm.service;

import com.x.hhqs.joa.alarm.vo.AlarmDefinitionInfoVO;

import java.util.List;

public interface AlarmDefinitionService {

    List<AlarmDefinitionInfoVO> getAllAlarmDefinitions();

    AlarmDefinitionInfoVO getAlarmDefinitionByAlarmTypeId(String alarmTypeId);
}
