package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.AlarmSeverity;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.ORIG_SEVERITY)
public class AlarmSeverityHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.ORIG_SEVERITY;
    }

    @Override
    public String getValue(XAlarmContext context) {
        String alarmSeverity = context.getXAlarmVO().getAlarmSeverity();
        if ("indeterminate".equals(alarmSeverity)) {
            return String.valueOf(AlarmSeverity.WARNING.getCode());
        }
        return String.valueOf(AlarmSeverity.fromValue(alarmSeverity).getCode());
    }
}