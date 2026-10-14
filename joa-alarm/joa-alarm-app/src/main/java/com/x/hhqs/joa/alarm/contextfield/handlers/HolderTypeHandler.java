package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import com.x.hhqs.joa.alarm.utils.HamUtils;
import jakarta.json.JsonObject;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@DefaultHandler(field = AlarmFields.HOLDER_TYPE)
public class HolderTypeHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.HOLDER_TYPE;
    }

    @Override
    public String getValue(XAlarmContext context) {
        if (context.isDeviceAlarm()) {
            return extractedHamAttr(context);
        }
        return EMPTY;
    }

    private String extractedHamAttr(XAlarmContext context) {
        String slotPlannedType = (String) context.getAttr(XAlarmContextFields.SLOT_PLANNED_TYPE);
        if (slotPlannedType == null) return EMPTY;
        return slotPlannedType;
    }
}