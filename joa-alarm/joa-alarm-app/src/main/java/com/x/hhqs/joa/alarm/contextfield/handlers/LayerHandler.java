package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import com.x.hhqs.joa.rmuid.generator.RmType;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.LAYER)
public class LayerHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.LAYER;
    }

    @Override
    public String getValue(XAlarmContext context) {
        if (context.isDeviceAlarm()) {
            String rmuidType = (String) context.getAttr(XAlarmContextFields.RMUID_TYPE);
            if (RmType.Port.getValue().equals(rmuidType)) {
                return "61";
            }
            return "0";
        } else {
            return "0";
        }
    }
}