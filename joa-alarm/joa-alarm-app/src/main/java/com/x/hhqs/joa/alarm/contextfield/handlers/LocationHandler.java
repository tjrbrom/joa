package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.LOCATION)
public class LocationHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.LOCATION;
    }

    @Override
    public String getValue(XAlarmContext context) {
        if (context.isDeviceAlarm()) {
            return null2Empty((String) context.getAttr(XAlarmContextFields.NE_LOCATION));
        }
        return EMPTY;
    }
}