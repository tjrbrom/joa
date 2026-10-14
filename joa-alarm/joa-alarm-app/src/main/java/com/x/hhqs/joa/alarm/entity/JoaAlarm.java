package com.x.hhqs.joa.alarm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.x.hhqs.joa.alarm.dto.JoaAlarmInfo;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

@TableName(value ="joa_alarm",autoResultMap = true)
public class JoaAlarm implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "alarm_seq")
    private Long alarmSeq;

    @TableField(value = "create_time")
    private Timestamp createTime;

    @TableField(value = "json_value", typeHandler = JacksonTypeHandler.class)
    private JoaAlarmInfo jsonValue;

    @TableField(value = "channel_index")
    private Integer channelIndex;

    @TableField(value = "alarm_id")
    private String alarmId;

    @TableField(value = "ne_name")
    private String neName;

    public JoaAlarm() {
    }

    public Long getId() {
        return id;
    }

    public JoaAlarm id(Long id) {
        this.id = id;
        return this;
    }

    public Long getAlarmSeq() {
        return alarmSeq;
    }

    public JoaAlarm alarmSeq(Long alarmSeq) {
        this.alarmSeq = alarmSeq;
        return this;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public JoaAlarm createTime(Timestamp createTime) {
        this.createTime = createTime;
        return this;
    }

    public JoaAlarmInfo getJsonValue() {
        return jsonValue;
    }

    public JoaAlarm jsonValue(JoaAlarmInfo jsonValue) {
        this.jsonValue = jsonValue;
        return this;
    }

    public Integer getChannelIndex() {
        return channelIndex;
    }

    public JoaAlarm channelIndex(Integer channelIndex) {
        this.channelIndex = channelIndex;
        return this;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public JoaAlarm alarmId(String alarmId) {
        this.alarmId = alarmId;
        return this;
    }

    public String getNeName() {
        return neName;
    }

    public JoaAlarm neName(String neName) {
        this.neName = neName;
        return this;
    }
}