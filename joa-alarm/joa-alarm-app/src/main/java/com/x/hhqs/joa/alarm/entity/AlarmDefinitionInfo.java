package com.x.hhqs.joa.alarm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value="AlarmDefinition")
public class AlarmDefinitionInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(value = "alarm_reporter")
    private AlarmReporter alarmReporter;
    @TableField(value = "ns_module_name")
    private String nsModuleName;
    @TableField(value = "alarm_type")
    private String alarmType;
    /**
     * unique constraint {"alarm_type_id"}
     */
    @TableField(value = "alarm_type_id")
    private String alarmTypeId;
    @TableField(value = "alarm_severity", exist = false)
    private AlarmSeverity severity;
    @TableField(value = "alarm_category")
    private String category;
    @TableField(value = "description")
    private String description;
    @TableField(value = "advice_for_repair", exist = false)
    private String adviceForRepair;
    @TableField(value = "additional_info", exist = false)
    private String additionalInfo;

    public AlarmDefinitionInfo() {
    }

    public Long getId() {
        return id;
    }

    public AlarmDefinitionInfo id(Long id) {
        this.id = id;
        return this;
    }

    public AlarmReporter getAlarmReporter() {
        return alarmReporter;
    }

    public AlarmDefinitionInfo alarmReporter(AlarmReporter alarmReporter) {
        this.alarmReporter = alarmReporter;
        return this;
    }

    public String getNsModuleName() {
        return nsModuleName;
    }

    public AlarmDefinitionInfo nsModuleName(String nsModuleName) {
        this.nsModuleName = nsModuleName;
        return this;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public AlarmDefinitionInfo alarmType(String alarmType) {
        this.alarmType = alarmType;
        return this;
    }

    public String getAlarmTypeId() {
        return alarmTypeId;
    }

    public AlarmDefinitionInfo alarmTypeId(String alarmTypeId) {
        this.alarmTypeId = alarmTypeId;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public AlarmDefinitionInfo category(String category) {
        this.category = category;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AlarmDefinitionInfo description(String description) {
        this.description = description;
        return this;
    }

    public AlarmSeverity getSeverity() {
        return severity;
    }

    public AlarmDefinitionInfo severity(AlarmSeverity severity) {
        this.severity = severity;
        return this;
    }

    public String getAdviceForRepair() {
        return adviceForRepair;
    }

    public AlarmDefinitionInfo adviceForRepair(String adviceForRepair) {
        this.adviceForRepair = adviceForRepair;
        return this;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public AlarmDefinitionInfo additionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }
}
