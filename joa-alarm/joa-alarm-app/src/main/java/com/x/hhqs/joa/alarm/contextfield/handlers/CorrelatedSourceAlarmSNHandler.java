package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.CORRELATED_SOURCE_ALARM_SN)
public class CorrelatedSourceAlarmSNHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.CORRELATED_SOURCE_ALARM_SN;
    }

    @Override
    public String getValue(XAlarmContext context) {
        return EMPTY;
    }
}