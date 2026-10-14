package com.x.hhqs.joa.alarm.listener.kafka.processor;

import com.x.hhqs.joa.alarm.entity.JoaAlarm;

import java.util.List;

public interface AlarmMessageProcessor {
    List<JoaAlarm> process(String message);
}
