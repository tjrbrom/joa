package com.x.hhqs.joa.alarm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.x.hhqs.joa.alarm.dto.JoaAlarmInfo;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.service.JoaAlarmDaoService;
import com.x.hhqs.joa.alarm.service.JoaAlarmService;
import com.x.hhqs.joa.alarm.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JoaAlarmServiceImpl implements JoaAlarmService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JoaAlarmServiceImpl.class);

    private final JoaAlarmDaoService joaAlarmDaoService;

    public JoaAlarmServiceImpl(JoaAlarmDaoService joaAlarmDaoService) {
        this.joaAlarmDaoService = joaAlarmDaoService;
    }

    @Override
    public List<String> findAllAlarmsByIds(List<String> alarmIds) {
        if (alarmIds == null || alarmIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<JoaAlarm> allJoaAlarms = joaAlarmDaoService.findAllAlarmsByIds(alarmIds);
        return allJoaAlarms.stream()
                .collect(Collectors.toMap(
                        JoaAlarm::getAlarmId,
                        Function.identity(),
                        (existing, replacement) -> replacement
                ))
                .values()
                .stream()
                .map(JoaAlarm::getJsonValue)
                .map(this::toJson)
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.toList());
    }

    private String toJson(JoaAlarmInfo alarm) {
        try {
            return JsonUtils.getInstance().toJson(alarm);
        } catch (JsonProcessingException e) {
            LOGGER.error("Mapper to json fail, alarm id=" + alarm.getAlarmId(), e);
            return null;
        }
    }
}
