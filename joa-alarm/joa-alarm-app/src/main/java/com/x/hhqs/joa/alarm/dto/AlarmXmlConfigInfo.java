package com.x.hhqs.joa.alarm.dto;

import com.x.hhqs.joa.alarm.entity.AlarmDefinitionInfo;
import com.x.hhqs.joa.alarm.entity.AlarmReporter;
import com.x.hhqs.joa.alarm.entity.AlarmSeverity;

public class AlarmXmlConfigInfo {
    private AlarmReporter alarmReporter;
    private String namespace;
    private String nsModuleName;

    private String alarmType;
    private String alarmTypeId;
    private String alarmTypeQualifier;

    private String description;
    private AlarmSeverity severity;
    private String adviceForRepair;
    private String additionalInfo;

    private String category;

    public AlarmReporter getAlarmReporter() {
        return alarmReporter;
    }

    public AlarmXmlConfigInfo alarmReporter(AlarmReporter alarmReporter) {
        this.alarmReporter = alarmReporter;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public AlarmXmlConfigInfo namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getNsModuleName() {
        return nsModuleName;
    }

    public AlarmXmlConfigInfo nsModuleName(String nsModuleName) {
        this.nsModuleName = nsModuleName;
        return this;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public AlarmXmlConfigInfo alarmType(String alarmType) {
        this.alarmType = alarmType;
        return this;
    }

    public String getAlarmTypeId() {
        return alarmTypeId;
    }

    public AlarmXmlConfigInfo alarmTypeId(String alarmTypeId) {
        this.alarmTypeId = alarmTypeId;
        return this;
    }

    public String getAlarmTypeQualifier() {
        return alarmTypeQualifier;
    }

    public AlarmXmlConfigInfo alarmTypeQualifier(String alarmTypeQualifier) {
        this.alarmTypeQualifier = alarmTypeQualifier;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AlarmXmlConfigInfo description(String description) {
        this.description = description;
        return this;
    }

    public AlarmSeverity getSeverity() {
        return severity;
    }

    public AlarmXmlConfigInfo severity(AlarmSeverity severity) {
        this.severity = severity;
        return this;
    }

    public String getAdviceForRepair() {
        return adviceForRepair;
    }

    public AlarmXmlConfigInfo adviceForRepair(String adviceForRepair) {
        this.adviceForRepair = adviceForRepair;
        return this;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public AlarmXmlConfigInfo additionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public AlarmXmlConfigInfo category(String category) {
        this.category = category;
        return this;
    }

    public AlarmDefinitionInfo toAlarmDefinition() {
        return new AlarmDefinitionInfo()
                .alarmReporter(this.alarmReporter)
                .nsModuleName(this.nsModuleName)
                .alarmType(this.alarmType)
                .alarmTypeId(this.alarmTypeId)
                .severity(this.severity)
                .category(this.category)
                .description(this.description)
                .adviceForRepair(this.adviceForRepair)
                .additionalInfo(this.additionalInfo)
                ;
    }

    @Override
    public String toString() {
        return "AlarmXmlConfigInfo{" +
                "alarmReporter=" + alarmReporter +
                ", namespace='" + namespace + '\'' +
                ", nsModuleName='" + nsModuleName + '\'' +
                ", alarmType='" + alarmType + '\'' +
                ", alarmTypeId='" + alarmTypeId + '\'' +
                ", alarmTypeQualifier='" + alarmTypeQualifier + '\'' +
                ", description='" + description + '\'' +
                ", severity='" + severity + '\'' +
                ", adviceForRepair='" + adviceForRepair + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
