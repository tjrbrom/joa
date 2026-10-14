package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.NE_NAME)
public class NeNameHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.NE_NAME;
    }

    @Override
    public String getValue(XAlarmContext context) {
        String neName = EMPTY;
        if (context.isDeviceAlarm()) {
            neName = null2Empty((String) context.getAttr(XAlarmContextFields.NE_NAME));
        } else {
            neName = null2Empty((String) context.getAttr(XAlarmContextFields.SITE_NAME));
        }
        return neName;
    }
}
