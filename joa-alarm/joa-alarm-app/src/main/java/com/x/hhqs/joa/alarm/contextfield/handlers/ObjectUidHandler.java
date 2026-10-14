package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import com.x.hhqs.joa.alarm.exception.UnsupportedAlarmException;
import com.x.hhqs.joa.alarm.utils.HamUtils;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObject;
import com.x.hhqs.joa.rmuid.service.RmUidService;
import jakarta.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@DefaultHandler(field = AlarmFields.OBJECT_UID)
public class ObjectUidHandler extends AbstractAttributeHandler {
    private static final Pattern DEVICE_PATTERN = Pattern.compile("^(/device=[^/]+)");

    @Autowired
    RmUidService rmUidService;

    @Override
    public String getId() {
        return AlarmFields.OBJECT_UID;
    }

    @Override
    public String getValue(XAlarmContext context) {
        String objectUid = EMPTY;
        if (context.isDeviceAlarm()) {
            JsonObject alarmJsonInfo = context.getAlarmJsonInfo();
            if (alarmJsonInfo != null &&
                    alarmJsonInfo.containsKey(XAlarmContextFields.HAM_OBJECT_ID) &&
                    alarmJsonInfo.containsKey(XAlarmContextFields.HAM_OBJECT_FRIENDLY_NAME)) {
                String hamObjectId = alarmJsonInfo.getString(XAlarmContextFields.HAM_OBJECT_ID);
                String hamObjectFriendlyName = alarmJsonInfo.getString(XAlarmContextFields.HAM_OBJECT_FRIENDLY_NAME);
                String rmuidType = (String) context.getAttr(XAlarmContextFields.RMUID_TYPE);
                String hamType = HamUtils.getInstance().parseHamTypeWithHamFn(hamObjectFriendlyName);
                HamObject hamObject = extractedHamAttr(context, hamObjectId, hamType, rmuidType);
                try {
                    objectUid = rmUidService.generateRmuid(hamObject);
                } catch (Exception e) {
                    String deviceHamId = extractDevicePath(hamObjectId);
                    hamObject = extractedHamAttr(context, deviceHamId, "Device", RmType.OLT.getValue());
                    try {
                        objectUid = rmUidService.generateRmuid(hamObject);
                    } catch (Exception ex) {
                        throw new UnsupportedAlarmException("Can not generate rmuid, alarm id: [" + context.getXAlarmVO().getAlarmId() + "], caused by: " + e.getMessage());
                    }
                }
            }
        } else {
            objectUid = rmUidService.getOmcRmUid();
        }
        return objectUid;
    }

    private HamObject extractedHamAttr(XAlarmContext context, String hamObjectId, String hamType, String rmuidType) {
        HamObject hamObject = new HamObject(hamObjectId, rmuidType);
        return switch (hamType) {
            case "Device", "Slot", "PON Port", "Cage", "Ethernet Port", "PON Link", "Network VLAN Interface", "LAG" -> {
                String neIp = (String) context.getAttr(XAlarmContextFields.NE_IP);
                hamObject.setDeviceIp(neIp);
                yield hamObject;
            }
            case "ONT", "UNI", "Service" -> {
                String neIp = (String) context.getAttr(XAlarmContextFields.NE_IP);
                hamObject.setDeviceIp(neIp);
                // TODO: retrieve onu id?
                yield hamObject;
            }
            default -> {
                yield hamObject;
            }
        };
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
