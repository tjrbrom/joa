package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.config.XAlarmProperties;
import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.ALARM_TYPE)
public class AlarmCategoryHandler extends AbstractAttributeHandler {
    private final XAlarmProperties xAlarmProperties;

    public AlarmCategoryHandler(XAlarmProperties xAlarmProperties) {
        this.xAlarmProperties = xAlarmProperties;
    }

    @Override
    public String getId() {
        return AlarmFields.ALARM_TYPE;
    }

    @Override
    public String getValue(XAlarmContext context) {
        return null2Empty(xAlarmProperties.getOmcAlarmType(context.getAlarmDefinition() != null ?
                context.getAlarmDefinition().getCategory() : null));
    }
}