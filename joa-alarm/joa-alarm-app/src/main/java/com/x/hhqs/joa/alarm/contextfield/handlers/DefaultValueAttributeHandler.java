package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.dto.XAlarmContext;

public class DefaultValueAttributeHandler implements AttributeHandler{
    public static final DefaultValueAttributeHandler EMPTY = new DefaultValueAttributeHandler();

    @Override
    public String getValue(XAlarmContext context) {
        return "";
    }
}
