package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import com.x.hhqs.joa.alarm.exception.UnsupportedAlarmException;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObject;
import com.x.hhqs.joa.rmuid.service.RmUidService;
import jakarta.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@DefaultHandler(field = AlarmFields.NE_UID)
public class NeUidHandler extends AbstractAttributeHandler {
    private static final Pattern DEVICE_PATTERN = Pattern.compile("^(/device=[^/]+)");

    @Autowired
    RmUidService rmUidService;

    @Override
    public String getId() {
        return AlarmFields.NE_UID;
    }

    @Override
    public String getValue(XAlarmContext context) {
        String neUID = EMPTY;
        if (context.isDeviceAlarm()) {
            JsonObject alarmJsonInfo = context.getAlarmJsonInfo();
            if (alarmJsonInfo != null && alarmJsonInfo.containsKey(XAlarmContextFields.HAM_OBJECT_ID)) {
                String hamObjectId = alarmJsonInfo.getString(XAlarmContextFields.HAM_OBJECT_ID);
                String deviceHamId = extractDevicePath(hamObjectId);
                HamObject hamObject = new HamObject(deviceHamId, RmType.OLT.getValue());
                String neIp = (String) context.getAttr(XAlarmContextFields.NE_IP);
                hamObject.setDeviceIp(neIp);
                try {
                    neUID = rmUidService.generateRmuid(hamObject);
                } catch (Exception e) {
                    throw new UnsupportedAlarmException("Can not generate rmuid, alarm id: [" + context.getXAlarmVO().getAlarmId() + "], caused by: " + e.getMessage());
                }
            }
        } else {
            neUID = rmUidService.getOmcRmUid();
        }
        return neUID;
    }

    /**
     * Extracts the /device=... part from a hamObjectId
     *
     * @param hamObjectId input string like "/device=realMF14_135.251.214.99/slot=LT-5"
     * @return only the "/device=..." part, or empty string if not found
     */
    public static String extractDevicePath(String hamObjectId) {
        if (hamObjectId == null) {
            return EMPTY;
        }
        Matcher matcher = DEVICE_PATTERN.matcher(hamObjectId);
        return matcher.find() ? matcher.group(1) : EMPTY;
    }
}
