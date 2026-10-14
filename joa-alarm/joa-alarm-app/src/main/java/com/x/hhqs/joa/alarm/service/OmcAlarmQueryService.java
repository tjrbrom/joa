package com.x.hhqs.joa.alarm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface OmcAlarmQueryService{

    List<OmcAlarmVO> getAlarmsStartWithId(Long id, int channelIndex);

    Integer countAlarmsStartWithId(Long id, int channelIndex);

    JoaAlarm getOmcAlarmByAlarmSeq(Long alarmSeq, int channelIndex);

    OmcAlarmVO makeOmcAlarmVO(JoaAlarm alarm) throws JsonProcessingException;

    List<OmcAlarmVO> getAlarmsDuringTime(Date startTime, Date endTime, int channelIndex);

    List<OmcAlarmVO> queryCurrentAlarms(OmcRequest request, int channelIndex);
}
