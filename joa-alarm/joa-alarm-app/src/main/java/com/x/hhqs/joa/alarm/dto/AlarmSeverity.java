package com.x.hhqs.joa.alarm.dto;

public enum AlarmSeverity {
    CRITICAL ("critical", 1),
    MAJOR ("major", 2),
    MINOR ("minor", 3),
    WARNING ("warning", 4);
    private final String name;
    private final int code;

    private AlarmSeverity(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static AlarmSeverity fromValue(String v) {
        for (AlarmSeverity e: AlarmSeverity.values()) {
            if (e.name.equals(v)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown alarm severity name: " + v);
    }

    public static AlarmSeverity fromValue(int code) {
        for (AlarmSeverity e: AlarmSeverity.values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown alarm severity code: " + code);
    }
}
