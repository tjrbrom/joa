package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.AlarmStatus;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.ALARM_STATUS)
public class AlarmStatusHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.ALARM_STATUS;
    }

    @Override
    public String getValue(XAlarmContext context) {
        String alarmStatus = context.getXAlarmVO().getAlarmStatus();
        return String.valueOf(AlarmStatus.fromValue(alarmStatus).getCode());
    }
}