package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.RELATE_TYPE)
public class RelateTypeHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.RELATE_TYPE;
    }

    @Override
    public String getValue(XAlarmContext context) {
        return "0";
    }
}