package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.ALARM_ID)
public class AlarmIdHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.ALARM_ID;
    }

    @Override
    public String getValue(XAlarmContext context) {
        return null2Empty(context.getXAlarmVO().getAlarmId());
    }
}