package com.x.hhqs.joa.alarm.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.x.hhqs.joa.alarm.entity.AlarmReporter;
import com.x.hhqs.joa.alarm.entity.AlarmSeverity;

public class AlarmDefinitionInfoVO {
    private Long id;
    private AlarmReporter alarmReporter;
    @JsonIgnore
    private String nsModuleName;
    @JsonIgnore
    private String alarmType;
    private String alarmTypeId;
    @JsonIgnore
    private AlarmSeverity severity;
    private String category;
    private String description;
    @JsonIgnore
    private String adviceForRepair;
    @JsonIgnore
    private String additionalInfo;

    public Long getId() {
        return id;
    }

    public AlarmDefinitionInfoVO id(Long id) {
        this.id = id;
        return this;
    }

    public AlarmReporter getAlarmReporter() {
        return alarmReporter;
    }

    public AlarmDefinitionInfoVO alarmReporter(AlarmReporter alarmReporter) {
        this.alarmReporter = alarmReporter;
        return this;
    }

    public String getNsModuleName() {
        return nsModuleName;
    }

    public AlarmDefinitionInfoVO nsModuleName(String nsModuleName) {
        this.nsModuleName = nsModuleName;
        return this;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public AlarmDefinitionInfoVO alarmType(String alarmType) {
        this.alarmType = alarmType;
        return this;
    }

    public String getAlarmTypeId() {
        return alarmTypeId;
    }

    public AlarmDefinitionInfoVO alarmTypeId(String alarmTypeId) {
        this.alarmTypeId = alarmTypeId;
        return this;
    }

    public AlarmSeverity getSeverity() {
        return severity;
    }

    public AlarmDefinitionInfoVO severity(AlarmSeverity severity) {
        this.severity = severity;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public AlarmDefinitionInfoVO category(String category) {
        this.category = category;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AlarmDefinitionInfoVO description(String description) {
        this.description = description;
        return this;
    }

    public String getAdviceForRepair() {
        return adviceForRepair;
    }

    public AlarmDefinitionInfoVO adviceForRepair(String adviceForRepair) {
        this.adviceForRepair = adviceForRepair;
        return this;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public AlarmDefinitionInfoVO additionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }
}
