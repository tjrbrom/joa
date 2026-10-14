package com.x.hhqs.joa.alarm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;

import java.util.List;
import java.util.Set;

public interface JoaAlarmDaoService extends IService<JoaAlarm> {
    List<JoaAlarm> findMaxSeqAlarmByChannelIndices(Set<Integer> channel_indices);

    List<JoaAlarm> findAllAlarmsByIds(List<String> alarmIds);
}
