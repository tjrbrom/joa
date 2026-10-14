package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.OBJECT_NAME)
public class ObjectNameHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.OBJECT_NAME;
    }

    @Override
    public String getValue(XAlarmContext context) {
        if (context.isDeviceAlarm()) {
            return context.getXAlarmVO().getAlarmResourceUiName();
        } else {
            if (context.hasAttr(XAlarmContextFields.SITE_NAME)) {
                return null2Empty((String) context.getAttr(XAlarmContextFields.SITE_NAME));
            }
            return EMPTY;
        }
    }
}
