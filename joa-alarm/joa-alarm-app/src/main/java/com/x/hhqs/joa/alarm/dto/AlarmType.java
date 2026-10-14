package com.x.hhqs.joa.alarm.dto;

public enum AlarmType {
    EquipmentAlarm,
    QualityOfServiceAlarm,
    CommunicationAlarm,
    EnvironmentalAlarm,
    ProcessingFailure,
    ;

    private AlarmType() {}

    public static AlarmType fromValue(String name) {
        for (AlarmType status: AlarmType.values()) {
            if (status.name().equals(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException(name);
    }
}
