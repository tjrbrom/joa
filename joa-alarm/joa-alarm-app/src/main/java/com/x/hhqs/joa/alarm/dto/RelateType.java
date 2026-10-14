package com.x.hhqs.joa.alarm.dto;

public enum RelateType {
    NORMAL(0),
    ROOT_CAUSE(1),
    DERIVED(2);

    private final int code;

    RelateType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RelateType fromValue(String v) {
        for (RelateType e: RelateType.values()) {
            if (e.name().equals(v)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown relate type name: " + v);
    }

    public static RelateType fromValue(int code) {
        for (RelateType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown relate type code: " + code);
    }
}
