package com.x.hhqs.joa.alarm.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum AlarmReporter {
    AC("AC", 1),
    AV("AV", 2),
    DEVICE("DEVICE", 3),
    UNKNOWN("UNKNOWN", 0);

    @JsonValue
    private final String name;
    @EnumValue
    private final int value;

    AlarmReporter(String name, int value) {
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
    public static AlarmReporter forValue(String value){
        if(StringUtils.isEmpty(value)) {
            return UNKNOWN;
        }
        return Arrays.stream(AlarmReporter.values()).filter(it->it.toString().equalsIgnoreCase(value)).findFirst()
                .orElse(UNKNOWN);
    }
}

