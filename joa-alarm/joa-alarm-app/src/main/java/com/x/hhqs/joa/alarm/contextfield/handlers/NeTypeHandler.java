package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import com.x.hhqs.joa.alarm.utils.HamUtils;
import jakarta.json.JsonObject;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.NE_TYPE)
public class NeTypeHandler extends AbstractAttributeHandler {
    private static final String OMC = "OMC";
    private static final String OLT = "OLT";
    private static final String ONU = "ONU";

    @Override
    public String getId() {
        return AlarmFields.NE_TYPE;
    }

    // OMC, OLT, ONU; UNI, Service -> ONU
    @Override
    public String getValue(XAlarmContext context) {
        String neType = EMPTY;
        if (context.isDeviceAlarm()) {
            JsonObject alarmJsonInfo = context.getAlarmJsonInfo();
            if (alarmJsonInfo != null && alarmJsonInfo.containsKey(XAlarmContextFields.HAM_OBJECT_FRIENDLY_NAME)) {
                String hamObjectFriendlyName = alarmJsonInfo.getString(XAlarmContextFields.HAM_OBJECT_FRIENDLY_NAME);
                String hamType = HamUtils.getInstance().parseHamTypeWithHamFn(hamObjectFriendlyName);
                neType = hamType2NeType(hamType);
            }
        } else {
            neType = OMC;
        }
        return neType;
    }
    
    public String hamType2NeType(String hamType) {
        return switch (hamType) {
            case "Device", "Slot", "Cage", "PON Port", "PON Link", "Network VLAN Interface", "Ethernet Port", "LAG" -> OLT;
            case "ONT", "UNI", "Service" -> ONU;
            default -> OLT;
        };
    }
}
