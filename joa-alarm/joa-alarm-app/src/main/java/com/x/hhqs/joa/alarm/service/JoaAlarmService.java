package com.x.hhqs.joa.alarm.service;

import java.util.List;

public interface JoaAlarmService {
    List<String> findAllAlarmsByIds(List<String> alarmIds);
}
