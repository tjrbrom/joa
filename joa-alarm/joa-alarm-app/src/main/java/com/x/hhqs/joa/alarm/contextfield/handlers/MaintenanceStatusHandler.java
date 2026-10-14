package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.MAINTENANCE_STATUS)
public class MaintenanceStatusHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.MAINTENANCE_STATUS;
    }

    @Override
    public String getValue(XAlarmContext context) {
        return EMPTY;
    }
}