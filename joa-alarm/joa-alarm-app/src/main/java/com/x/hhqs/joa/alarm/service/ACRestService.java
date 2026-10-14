package com.x.hhqs.joa.alarm.service;

import com.x.hhqs.joa.alarm.dto.AlarmTypeInfo;
import com.x.hhqs.joa.common.restclient.Pair;

import java.util.List;

public interface ACRestService {
    List<Pair<String, String>> getAllManagerNameTypePairs();
    List<AlarmTypeInfo> getAllSupportedAlarmTypesByCoreTarget(String target);
    String retrieveCoreSiteName();
}
