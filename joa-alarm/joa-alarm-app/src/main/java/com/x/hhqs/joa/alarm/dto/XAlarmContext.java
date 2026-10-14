package com.x.hhqs.joa.alarm.dto;

import com.x.hhqs.joa.alarm.vo.AlarmDefinitionInfoVO;
import jakarta.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class XAlarmContext {
    private XAlarmVO xAlarmVO;
    private AlarmDefinitionInfoVO alarmDefinition;
    private JsonObject alarmJsonInfo;
    private boolean isDeviceAlarm;
    private Map<String, Object> addAttrs;

    private XAlarmContext(XAlarmVO xAlarmVO, AlarmDefinitionInfoVO alarmDefinition, JsonObject alarmJsonInfo) {
        this.xAlarmVO = xAlarmVO;
        this.alarmDefinition = alarmDefinition;
        this.alarmJsonInfo = alarmJsonInfo;
        this.addAttrs = new HashMap<>();
        this.isDeviceAlarm = false;
    }

    public static XAlarmContext of(XAlarmVO xAlarmVO, AlarmDefinitionInfoVO alarmDefinition, JsonObject alarmJsonInfo) {
        return new XAlarmContext(xAlarmVO, alarmDefinition, alarmJsonInfo);
    }

    public void addAttr(String key, Object v) {
        this.addAttrs.putIfAbsent(key, v);
    }

    public Object getAttr(String key) {
        return this.addAttrs.get(key);
    }

    public boolean hasAttr(String key) {
        return this.addAttrs.containsKey(key);
    }
}
