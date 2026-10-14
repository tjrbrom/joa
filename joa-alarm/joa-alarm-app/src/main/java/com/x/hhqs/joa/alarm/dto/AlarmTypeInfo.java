package com.x.hhqs.joa.alarm.dto;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AlarmTypeInfo {
    private String alarmTypeId;
    private String alarmTypeQualifier;
    private Set<String> alarmTypeReporter = new HashSet<>();
    private String friendlyName;

    public AlarmTypeInfo(String alarmTypeId, String alarmTypeQualifier, String friendlyName) {
        this.alarmTypeId = alarmTypeId;
        this.alarmTypeQualifier = alarmTypeQualifier;
        this.friendlyName = friendlyName;
    }

    public AlarmTypeInfo(){}

    public String getAlarmTypeId() {
        return alarmTypeId;
    }

    public void setAlarmTypeId(String alarmTypeId) {
        this.alarmTypeId = alarmTypeId;
    }

    public String getAlarmTypeQualifier() {
        return alarmTypeQualifier;
    }

    public void setAlarmTypeQualifier(String alarmTypeQualifier) {
        this.alarmTypeQualifier = alarmTypeQualifier;
    }

    public Set<String> getAlarmTypeReporter() {
        return alarmTypeReporter;
    }

    public void setAlarmTypeReporter(Set<String> alarmTypeReporter) {
        this.alarmTypeReporter = alarmTypeReporter;
    }

    public String getFriendlyName() { return friendlyName; }

    public void setFriendlyName(String friendlyName) { this.friendlyName = friendlyName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlarmTypeInfo that = (AlarmTypeInfo) o;
        return Objects.equals(alarmTypeId, that.alarmTypeId) &&
                Objects.equals(alarmTypeQualifier, that.alarmTypeQualifier) &&
                Objects.equals(friendlyName, that.friendlyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alarmTypeId, alarmTypeQualifier, friendlyName);
    }
}