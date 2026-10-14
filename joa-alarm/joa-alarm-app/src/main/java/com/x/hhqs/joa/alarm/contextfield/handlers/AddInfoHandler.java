package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.utils.AlarmFieldUtil;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.ADDINFO)
public class AddInfoHandler extends AbstractAttributeHandler {
    public static final String DETAIL = "detail";
    @Override
    public String getId() {
        return AlarmFields.ADDINFO;
    }

    @Override
    public String getValue(XAlarmContext context) {
        String alarmText = context.getXAlarmVO().getAlarmText();
        return convert(alarmText);
    }

    /**
     *
     * @param alarmText Metric: KPI_XPON_VOLTAGE_UNIT_utilization, Threshold: > 90, Actual Value: 319.74, KPI Model: LS-MF, Family: LS-MF-LWLT-C-23.12
     * @return detail:Metric# KPI_XPON_VOLTAGE_UNIT_utilization, Threshold# > 90, Actual Value# 319.74, KPI Model# LS-MF, Family# LS-MF-LWLT-C-23.12
     */
    protected static String convert(String alarmText) {
        if (alarmText == null || alarmText.trim().isEmpty()) {
            return DETAIL + ":" + EMPTY;
        }
        return DETAIL + ":" + AlarmFieldUtil.escapeSpecialCharacters(alarmText.replaceAll(":", "#"));
    }
}