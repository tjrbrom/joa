package com.x.hhqs.joa.alarm.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum AlarmSeverity {

    UNKNOWN("unknown", 0),
    CLEAR ("cleared", 1),
    INDETERMINATE ("indeterminate", 2),
    MINOR ("minor", 3),
    WARNING ("warning", 4),
    MAJOR ("major", 5),
    CRITICAL ("critical", 6);

    @JsonValue
    private final String name;
    @EnumValue
    private final int value;

    AlarmSeverity(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String toString() {
        return name;
    }

    public int value() {
        return value;
    }


    @JsonCreator
    public static AlarmSeverity forValue(String strVal){
        if(StringUtils.isEmpty(strVal)) {
            return UNKNOWN;
        }
        return Arrays.stream(AlarmSeverity.values()).filter(it->it.toString().equalsIgnoreCase(strVal)).findFirst()
                .orElse(UNKNOWN);
    }
}

