package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.utils.AlarmFieldUtil;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.LOCATION_INFO)
public class LocationInfoHandler extends AbstractAttributeHandler {
    @Override
    public String getId() {
        return AlarmFields.LOCATION_INFO;
    }

    @Override
    public String getValue(XAlarmContext context) {
        String alarmResourceUiName = context.getXAlarmVO().getAlarmResourceUiName();
        return convert(alarmResourceUiName);
    }

    /**
     *
     * @param alarmResourceUiName exportingProcess:DH247.LT1:AP-ipfix-exporting-process
     * @return exportingProcess:DH247.LT1#AP-ipfix-exporting-process
     */
    protected static String convert(String alarmResourceUiName) {
        if (alarmResourceUiName == null || alarmResourceUiName.trim().isEmpty()) {
            return EMPTY;
        }

        String[] names = alarmResourceUiName.split("[:]", 2);
        if (names.length == 1) {
            return alarmResourceUiName;
        }

        return AlarmFieldUtil.escapeSpecialCharacters(names[0]) + ":" + AlarmFieldUtil.escapeSpecialCharacters(names[1].replaceAll(":", "#"));
    }
}