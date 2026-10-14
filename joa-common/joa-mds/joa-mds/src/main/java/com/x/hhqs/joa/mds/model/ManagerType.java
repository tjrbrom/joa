package com.x.hhqs.joa.mds.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ManagerType {
    AMS,
    NAV,
    LOCAL,
    UNKNOWN;

    @JsonCreator
    public static ManagerType fromString(String key) {
        try {
            return ManagerType.valueOf(key.toUpperCase());
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
