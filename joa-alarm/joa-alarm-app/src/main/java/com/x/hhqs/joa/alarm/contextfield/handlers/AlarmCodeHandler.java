package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = { AlarmFields.ALARM_TITLE, AlarmFields.SPECIFIC_PROBLEM_ID })
public class AlarmCodeHandler extends AbstractAttributeHandler {
    private String id = "alarmCode";

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getValue(XAlarmContext context) {
        return null2Empty(context.getXAlarmVO().getAlarmTypeId());
    }
}
