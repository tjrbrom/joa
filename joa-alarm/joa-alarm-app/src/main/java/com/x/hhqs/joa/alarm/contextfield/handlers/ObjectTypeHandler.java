package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.OBJECT_TYPE)
public class ObjectTypeHandler extends AbstractAttributeHandler {
    public static final String OMC = "OMC";
    @Override
    public String getId() {
        return AlarmFields.OBJECT_TYPE;
    }

    @Override
    public String getValue(XAlarmContext context) {
        if (context.isDeviceAlarm()) {
            return null2Empty((String) context.getAttr(XAlarmContextFields.RMUID_TYPE));
        } else {
            return OMC;
        }
    }
}
