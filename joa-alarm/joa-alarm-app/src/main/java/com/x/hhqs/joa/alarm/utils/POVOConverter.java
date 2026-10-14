package com.x.hhqs.joa.alarm.utils;

import com.x.hhqs.joa.alarm.entity.AlarmDefinitionInfo;
import com.x.hhqs.joa.alarm.vo.AlarmDefinitionInfoVO;

public class POVOConverter {
    public static AlarmDefinitionInfoVO po2vo(AlarmDefinitionInfo po) {
        return new AlarmDefinitionInfoVO()
                .id(po.getId())
                .alarmReporter(po.getAlarmReporter())
                .nsModuleName(po.getNsModuleName())
                .alarmType(po.getAlarmType())
                .alarmTypeId(po.getAlarmTypeId())
                .severity(po.getSeverity())
                .category(po.getCategory())
                .description(po.getDescription())
                .adviceForRepair(po.getAdviceForRepair())
                .additionalInfo(po.getAdditionalInfo())
                ;
    }

    public static AlarmDefinitionInfo vo2po(AlarmDefinitionInfoVO vo) {
        return new AlarmDefinitionInfo()
                .id(vo.getId())
                .alarmReporter(vo.getAlarmReporter())
                .nsModuleName(vo.getNsModuleName())
                .alarmType(vo.getAlarmType())
                .alarmTypeId(vo.getAlarmTypeId())
                .severity(vo.getSeverity())
                .category(vo.getCategory())
                .description(vo.getDescription())
                .adviceForRepair(vo.getAdviceForRepair())
                .additionalInfo(vo.getAdditionalInfo())
                ;
    }
}
