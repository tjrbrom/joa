package com.x.hhqs.joa.alarm.vo;

public enum ManagerType {
    AMS, NAV, LOCAL, REST, X_ACS, GAC, GENERIC_ACS, NSP, NSP_MDM, NSP_NFMP, CLI, AC, IBN;

    public String getManagerType() {
        return this.name();
    }
}