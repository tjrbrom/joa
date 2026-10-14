package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.SPECIFIC_PROBLEM)
public class SpecificProblemHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.SPECIFIC_PROBLEM;
    }

    @Override
    public String getValue(XAlarmContext context) {
        return null2Empty(context.getAlarmDefinition() != null ? context.getAlarmDefinition().getDescription() : null);
    }
}