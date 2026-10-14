package com.x.hhqs.joa.alarm.dto;

public enum AlarmStatus {
    Active(1), Cleared(0);

    private final int code;

    private AlarmStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AlarmStatus fromValue(String v) {
        for (AlarmStatus status: AlarmStatus.values()) {
            if (status.name().equals(v)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown alarm status name: " + v);
    }

    public static AlarmStatus fromValue(int code) {
        for (AlarmStatus status: AlarmStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}